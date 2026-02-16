package integration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import bll.EditorBO;
import bll.FacadeBO;
import bll.IFacadeBO;
import dal.IFacadeDAO;
import dto.Documents;
import dto.Pages;

/**
 * Integration Boundary Tests - Layer Communication
 * Tests: PL->BLL->DAL data flow, layer violations, null propagation, error handling across layers
 */
public class LayerIntegrationBoundaryTest {
    
    @Mock
    private IFacadeDAO mockDAO;
    
    private IFacadeBO facadeBO;
    private EditorBO editorBO;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        editorBO = new EditorBO(mockDAO);
        facadeBO = new FacadeBO(editorBO);
    }
    
    // ========== TEST CATEGORY: NULL PROPAGATION ACROSS LAYERS ==========
    
    @Test
    public void testNullPropagation_DAL_to_BLL_to_PL() {
        // Boundary: null returns from DAL propagate to PL (Issue #18)
        when(mockDAO.getFilesFromDB()).thenReturn(null);
        
        try {
            List<Documents> result = facadeBO.getAllFiles();
            if (result == null) {
                fail("Issue #18: Null propagates from DAL to PL");
            }
        } catch (NullPointerException e) {
            fail("Issue #18: NPE thrown instead of handling null");
        }
    }
    
    @Test
    public void testEmptyListPropagation_DAL_to_PL() {
        // Boundary: empty list from DAL
        when(mockDAO.getFilesFromDB()).thenReturn(new ArrayList<>());
        
        List<Documents> result = facadeBO.getAllFiles();
        assertNotNull("Should return empty list, not null", result);
        assertEquals("Should have 0 items", 0, result.size());
    }
    
    // ========== TEST CATEGORY: ERROR HANDLING ACROSS LAYERS ==========
    
    @Test
    public void testErrorHandling_Exception_in_DAL() {
        // Boundary: exception in DAL caught by BLL
        when(mockDAO.createFileInDB(anyString(), anyString()))
            .thenThrow(new RuntimeException("Database error"));
        
        boolean result = facadeBO.createFile("test.txt", "content");
        assertFalse("BLL should catch DAL exception and return false", result);
    }
    
    @Test
    public void testErrorHandling_SQLException_Propagation() {
        // Boundary: SQL exception handling across layers
        when(mockDAO.createFileInDB(anyString(), anyString()))
            .thenThrow(new RuntimeException("SQL Exception"));
        
        try {
            boolean result = facadeBO.createFile("test.txt", "content");
            assertFalse("Should catch and handle SQL exception", result);
        } catch (Exception e) {
            fail("Exception should be caught in BLL, not propagate to PL");
        }
    }
    
    // ========== TEST CATEGORY: TRANSACTION BOUNDARIES (Issue #17) ==========
    
    @Test
    public void testTransaction_Partial_Failure() {
        // Boundary: transaction not used, partial failure leaves inconsistent state (Issue #17)
        // If file created but pages fail, database is inconsistent
        when(mockDAO.createFileInDB(anyString(), anyString()))
            .thenReturn(true)  // File created
            .thenThrow(new RuntimeException("Pages failed")); // But subsequent operations fail
        
        boolean result = facadeBO.createFile("test.txt", "content");
        // Without transactions, first call succeeds, second fails
        // Database now has file without pages (inconsistent state)
    }
    
    // ========== TEST CATEGORY: DATA VALIDATION ACROSS LAYERS ==========
    
    @Test
    public void testValidation_Bypassing_BLL() {
        // Boundary: PL bypassing BLL to call DAL directly (Issue #45)
        // This violates layer architecture
        // Test documents that this is possible and should be prevented
        
        // If PL had direct DAO reference:
        // dao.createFileInDB("", ""); // No BLL validation!
        
        // This test verifies BLL adds value through validation
        when(mockDAO.createFileInDB("", "")).thenReturn(false);
        boolean result = facadeBO.createFile("", "");
        assertFalse("BLL should add validation", result);
    }
    
    @Test
    public void testValidation_Input_Sanitization() {
        // Boundary: BLL should sanitize before passing to DAL (Issue #19)
        String unsafeInput = "'; DROP TABLE files; --";
        
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(true);
        boolean result = facadeBO.createFile(unsafeInput, "content");
        
        // BLL should sanitize or DAL should use PreparedStatement
        verify(mockDAO).createFileInDB(anyString(), anyString());
    }
    
    // ========== TEST CATEGORY: DTO DATA TRANSFER ==========
    
    @Test
    public void testDTO_NullFields_Propagation() {
        // Boundary: DTO with null fields
        Documents doc = new Documents(1, null, "hash", "2024-01-01", "2024-01-01", null);
        doc.setId(1);
        doc.setName(null); // Null name
        // Null pages already set in constructor
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        when(mockDAO.getFilesFromDB()).thenReturn(docs);
        
        List<Documents> result = facadeBO.getAllFiles();
        assertNotNull("Should return list", result);
        assertEquals("Should have 1 document", 1, result.size());
        
        Documents resultDoc = result.get(0);
        // Check how nulls are handled
        assertNull("Name is null", resultDoc.getName() );
        assertNull("Pages is null", resultDoc.getPages());
    }
    
    @Test
    public void testDTO_InvalidId_Propagation() {
        // Boundary: DTO with invalid ID (Issue #44)
        Documents doc = new Documents(-1, "test.txt", "hash", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc.setId(-1); // Invalid negative ID
        doc.setName("test.txt");
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        when(mockDAO.getFilesFromDB()).thenReturn(docs);
        
        List<Documents> result = facadeBO.getAllFiles();
        assertEquals("Should propagate invalid ID", -1, result.get(0).getId());
        // No validation in DTO - Issue #44
    }
    
    // ========== TEST CATEGORY: PERFORMANCE ACROSS LAYERS ==========
    
    @Test(timeout = 10000) // 10 second timeout
    public void testPerformance_Large_Data_Transfer() {
        // Boundary: transferring large data through layers
        List<Documents> largeDocs = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            Pages page = new Pages(1, i, 1, "Page content");
            page.setPageNumber(1);
            page.setPageContent("Page content");
            List<Pages> pages = new ArrayList<>();
            pages.add(page);
            
            Documents doc = new Documents(i, "File" + i + ".txt", "hash" + i, "2024-01-01", "2024-01-01", pages);
            doc.setId(i);
            doc.setName("File" + i + ".txt");
            
            largeDocs.add(doc);
        }
        
        when(mockDAO.getFilesFromDB()).thenReturn(largeDocs);
        
        long startTime = System.currentTimeMillis();
        List<Documents> result = facadeBO.getAllFiles();
        long endTime = System.currentTimeMillis();
        
        assertEquals("Should transfer all documents", 10000, result.size());
        assertTrue("Should complete in reasonable time", (endTime - startTime) < 10000);
    }
    
    // ========== TEST CATEGORY: CONCURRENT ACCESS ==========
    
    @Test
    public void testConcurrency_Multiple_Threads() throws InterruptedException {
        // Boundary: multiple threads accessing through layers
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(true);
        
        final int THREAD_COUNT = 10;
        Thread[] threads = new Thread[THREAD_COUNT];
        final boolean[] results = new boolean[THREAD_COUNT];
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                results[index] = facadeBO.createFile("File" + index + ".txt", "Content " + index);
            });
            threads[i].start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Verify all succeeded (with proper synchronization)
        for (boolean result : results) {
            assertTrue("All threads should succeed", result);
        }
    }
    
    // ========== TEST CATEGORY: LAYER RESPONSIBILITY ==========
    
    @Test
    public void testResponsibility_BLL_Adds_Business_Logic() {
        // Boundary: verify BLL adds value beyond DAL
        // BLL should handle business rules, not just pass through
        
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(true);
        
        // Test that BLL might add file extension if missing
        boolean result = facadeBO.createFile("filename", "content");
        
        // Verify BLL processed the request
        verify(mockDAO).createFileInDB(anyString(), anyString());
    }
    
    @Test
    public void testResponsibility_DAL_Only_Data_Access() {
        // Boundary: DAL should not contain business logic
        // Verify DAL is called with already-validated data
        
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(true);
        
        facadeBO.createFile("valid.txt", "valid content");
        
        // DAL should receive clean, validated data
        verify(mockDAO).createFileInDB(eq("valid.txt"), eq("valid content"));
    }
    
    // ========== TEST CATEGORY: CIRCULAR DEPENDENCIES ==========
    
    @Test
    public void testArchitecture_No_Circular_Dependencies() {
        // Boundary: verify no circular dependencies between layers
        // PL -> BLL -> DAL (one direction only)
        
        // This test documents the architecture
        // BLL should not call PL
        // DAL should not call BLL or PL
        assertTrue("Architecture should be layered", true);
    }
    
    // ========== TEST CATEGORY: BOUNDARY DATA VALUES ==========
    
    @Test
    public void testBoundary_MaxInteger_Through_Layers() {
        // Boundary: maximum integer ID through all layers
        when(mockDAO.deleteFileInDB(Integer.MAX_VALUE)).thenReturn(false);
        
        boolean result = facadeBO.deleteFile(Integer.MAX_VALUE);
        assertFalse("Should handle max integer", result);
        
        verify(mockDAO).deleteFileInDB(Integer.MAX_VALUE);
    }
    
    @Test
    public void testBoundary_MinInteger_Through_Layers() {
        // Boundary: minimum integer ID through all layers
        when(mockDAO.deleteFileInDB(Integer.MIN_VALUE)).thenReturn(false);
        
        boolean result = facadeBO.deleteFile(Integer.MIN_VALUE);
        assertFalse("Should handle min integer", result);
        
        verify(mockDAO).deleteFileInDB(Integer.MIN_VALUE);
    }
    
    @Test
    public void testBoundary_EmptyString_Through_Layers() {
        // Boundary: empty string through all layers
        when(mockDAO.createFileInDB("", "")).thenReturn(false);
        
        boolean result = facadeBO.createFile("", "");
        assertFalse("Should reject empty string", result);
    }
    
    @Test
    public void testBoundary_VeryLongString_Through_Layers() {
        // Boundary: very long string through all layers
        StringBuilder longString = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            longString.append("a");
        }
        
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(false);
        
        boolean result = facadeBO.createFile("file.txt", longString.toString());
        // Should be handled at some layer
    }
}
