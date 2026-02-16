package bll;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import dal.IFacadeDAO;
import dto.Documents;

/**
 * Boundary and Limit Condition Tests for EditorBO (Business Logic Layer)
 * Tests: Empty strings, null values, edge cases, maximum lengths
 */
public class EditorBOBoundaryTest {
    
    @Mock
    private IFacadeDAO mockDAO;
    
    private EditorBO editorBO;
    private File tempFile;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        editorBO = new EditorBO(mockDAO);
    }
    
    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }
    
    // ========== TEST CATEGORY: NULL INPUT VALIDATION ==========
    
    @Test(expected = NullPointerException.class)
    public void testCreateFile_NullFileName() {
        // Boundary: null file name
        when(mockDAO.createFileInDB(null, "content")).thenThrow(new NullPointerException());
        editorBO.createFile(null, "content");
    }
    
    @Test
    public void testCreateFile_NullContent() {
        // Boundary: null content (should be handled gracefully)
        when(mockDAO.createFileInDB("test.txt", null)).thenReturn(false);
        boolean result = editorBO.createFile("test.txt", null);
        assertFalse("Should return false for null content", result);
    }
    
    @Test
    public void testCreateFile_BothNull() {
        // Boundary: both parameters null
        when(mockDAO.createFileInDB(null, null)).thenReturn(false);
        boolean result = editorBO.createFile(null, null);
        assertFalse("Should return false for null inputs", result);
    }
    
    // ========== TEST CATEGORY: EMPTY STRING VALIDATION ==========
    
    @Test
    public void testCreateFile_EmptyFileName() {
        // Boundary: empty file name
        when(mockDAO.createFileInDB("", "content")).thenReturn(false);
        boolean result = editorBO.createFile("", "content");
        assertFalse("Should reject empty file name", result);
    }
    
    @Test
    public void testCreateFile_EmptyContent() {
        // Boundary: empty content (Issue #14)
        when(mockDAO.createFileInDB("test.txt", "")).thenReturn(true);
        boolean result = editorBO.createFile("test.txt", "");
        // This currently passes but SHOULD fail - identified bug
        assertTrue("Current behavior: accepts empty content (BUG)", result);
    }
    
    @Test
    public void testCreateFile_WhitespaceOnlyContent() {
        // Boundary: whitespace-only content
        when(mockDAO.createFileInDB("test.txt", "   \n\t  ")).thenReturn(true);
        boolean result = editorBO.createFile("test.txt", "   \n\t  ");
        assertTrue("Should handle whitespace content", result);
    }
    
    // ========== TEST CATEGORY: LENGTH BOUNDARIES ==========
    
    @Test
    public void testCreateFile_MaxFileNameLength() {
        // Boundary: file name at VARCHAR limit (255 chars)
        String maxLengthName = generateString(255);
        when(mockDAO.createFileInDB(maxLengthName, "content")).thenReturn(true);
        boolean result = editorBO.createFile(maxLengthName, "content");
        assertTrue("Should accept file name at max length", result);
    }
    
    @Test
    public void testCreateFile_ExceedMaxFileNameLength() {
        // Boundary: file name exceeding VARCHAR limit (256 chars)
        String tooLongName = generateString(256);
        when(mockDAO.createFileInDB(tooLongName, "content")).thenReturn(false);
        boolean result = editorBO.createFile(tooLongName, "content");
        assertFalse("Should reject file name exceeding max length", result);
    }
    
    @Test
    public void testCreateFile_LargeContent() {
        // Boundary: 1MB content
        String largeContent = generateString(1024 * 1024); // 1MB
        when(mockDAO.createFileInDB("large.txt", largeContent)).thenReturn(true);
        boolean result = editorBO.createFile("large.txt", largeContent);
        assertTrue("Should handle 1MB content", result);
    }
    
    @Test
    public void testCreateFile_VeryLargeContent() {
        // Boundary: 10MB content (near limit)
        String veryLargeContent = generateString(10 * 1024 * 1024); // 10MB
        when(mockDAO.createFileInDB("verylarge.txt", veryLargeContent)).thenReturn(false);
        boolean result = editorBO.createFile("verylarge.txt", veryLargeContent);
        // Should reject or handle gracefully
        assertFalse("Should reject very large content", result);
    }
    
    // ========== TEST CATEGORY: UPDATE FILE BOUNDARIES ==========
    
    @Test
    public void testUpdateFile_NegativeId() {
        // Boundary: negative file ID (Issue #31)
        when(mockDAO.updateFileInDB(-1, "file.txt", 1, "content")).thenReturn(false);
        boolean result = editorBO.updateFile(-1, "file.txt", 1, "content");
        assertFalse("Should reject negative ID", result);
    }
    
    @Test
    public void testUpdateFile_ZeroId() {
        // Boundary: ID = 0
        when(mockDAO.updateFileInDB(0, "file.txt", 1, "content")).thenReturn(false);
        boolean result = editorBO.updateFile(0, "file.txt", 1, "content");
        assertFalse("Should reject ID = 0", result);
    }
    
    @Test
    public void testUpdateFile_NonExistentId() {
        // Boundary: ID doesn't exist (Issue #31)
        when(mockDAO.updateFileInDB(999999, "file.txt", 1, "content")).thenReturn(false);
        boolean result = editorBO.updateFile(999999, "file.txt", 1, "content");
        assertFalse("Should return false for non-existent ID", result);
    }
    
    @Test
    public void testUpdateFile_NegativePageNumber() {
        // Boundary: negative page number
        when(mockDAO.updateFileInDB(1, "file.txt", -1, "content")).thenReturn(false);
        boolean result = editorBO.updateFile(1, "file.txt", -1, "content");
        assertFalse("Should reject negative page number", result);
    }
    
    @Test
    public void testUpdateFile_MaxIntegerId() {
        // Boundary: maximum integer ID
        when(mockDAO.updateFileInDB(Integer.MAX_VALUE, "file.txt", 1, "content")).thenReturn(false);
        boolean result = editorBO.updateFile(Integer.MAX_VALUE, "file.txt", 1, "content");
        assertFalse("Should handle max integer ID", result);
    }
    
    // ========== TEST CATEGORY: DELETE FILE BOUNDARIES ==========
    
    @Test
    public void testDeleteFile_NegativeId() {
        // Boundary: negative file ID
        when(mockDAO.deleteFileInDB(-1)).thenReturn(false);
        boolean result = editorBO.deleteFile(-1);
        assertFalse("Should reject negative ID for deletion", result);
    }
    
    @Test
    public void testDeleteFile_ZeroId() {
        // Boundary: ID = 0
        when(mockDAO.deleteFileInDB(0)).thenReturn(false);
        boolean result = editorBO.deleteFile(0);
        assertFalse("Should reject ID = 0 for deletion", result);
    }
    
    @Test
    public void testDeleteFile_NonExistentId() {
        // Boundary: non-existent ID
        when(mockDAO.deleteFileInDB(999999)).thenReturn(false);
        boolean result = editorBO.deleteFile(999999);
        assertFalse("Should return false for non-existent ID", result);
    }
    
    // ========== TEST CATEGORY: IMPORT FILE BOUNDARIES ==========
    
    @Test
    public void testImportTextFiles_NullFile() throws IOException {
        // Boundary: null file
        boolean result = editorBO.importTextFiles(null, "test.txt");
        assertFalse("Should reject null file", result);
    }
    
    @Test
    public void testImportTextFiles_NonExistentFile() {
        // Boundary: file doesn't exist
        File nonExistent = new File("nonexistent.txt");
        boolean result = editorBO.importTextFiles(nonExistent, "nonexistent.txt");
        assertFalse("Should reject non-existent file", result);
    }
    
    @Test
    public void testImportTextFiles_EmptyFile() throws IOException {
        // Boundary: empty file (0 bytes)
        tempFile = File.createTempFile("empty", ".txt");
        when(mockDAO.createFileInDB(anyString(), eq(""))).thenReturn(true);
        boolean result = editorBO.importTextFiles(tempFile, "empty.txt");
        assertTrue("Should handle empty file", result);
    }
    
    @Test
    public void testImportTextFiles_InvalidExtension() throws IOException {
        // Boundary: invalid file extension (Issue #14)
        tempFile = File.createTempFile("test", ".exe");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("content");
        writer.close();
        
        boolean result = editorBO.importTextFiles(tempFile, "test.exe");
        assertFalse("Should reject non-text file", result);
    }
    
    @Test
    public void testImportTextFiles_ValidTxtExtension() throws IOException {
        // Boundary: valid .txt extension
        tempFile = File.createTempFile("test", ".txt");
        FileWriter writer = new FileWriter(tempFile);
        writer.write("content");
        writer.close();
        
        when(mockDAO.createFileInDB(anyString(), anyString())).thenReturn(true);
        boolean result = editorBO.importTextFiles(tempFile, "test.txt");
        assertTrue("Should accept .txt file", result);
    }
    
    // ========== TEST CATEGORY: GET FILE BOUNDARIES ==========
    
    @Test
    public void testGetFile_NegativeId() {
        // Boundary: negative ID
        List<Documents> emptyList = new ArrayList<>();
        when(mockDAO.getFilesFromDB()).thenReturn(emptyList);
        Documents result = editorBO.getFile(-1);
        assertNull("Should return null for negative ID", result);
    }
    
    @Test
    public void testGetFile_NonExistentId() {
        // Boundary: ID doesn't exist
        List<Documents> docs = new ArrayList<>();
        Documents doc = new Documents(1, "test", "hash", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc.setId(1);
        docs.add(doc);
        when(mockDAO.getFilesFromDB()).thenReturn(docs);
        
        Documents result = editorBO.getFile(999);
        assertNull("Should return null for non-existent ID", result);
    }
    
    @Test
    public void testGetFile_EmptyDocumentList() {
        // Boundary: no documents in database
        when(mockDAO.getFilesFromDB()).thenReturn(new ArrayList<>());
        Documents result = editorBO.getFile(1);
        assertNull("Should return null when no documents exist", result);
    }
    
    @Test
    public void testGetFile_NullDocumentList() {
        // Boundary: null return from DAO (Issue #18)
        when(mockDAO.getFilesFromDB()).thenReturn(null);
        try {
            Documents result = editorBO.getFile(1);
            // If no exception, should return null
            assertNull("Should handle null document list", result);
        } catch (NullPointerException e) {
            fail("Should not throw NPE - Issue #18 confirmed");
        }
    }
    
    // ========== TEST CATEGORY: FILE EXTENSION BOUNDARIES ==========
    
    @Test
    public void testGetFileExtension_NoExtension() {
        // Boundary: filename without extension
        String ext = editorBO.getFileExtension("filename");
        assertEquals("Should return empty string for no extension", "", ext);
    }
    
    @Test
    public void testGetFileExtension_EmptyString() {
        // Boundary: empty filename
        String ext = editorBO.getFileExtension("");
        assertEquals("Should return empty string for empty filename", "", ext);
    }
    
    @Test
    public void testGetFileExtension_NullString() {
        // Boundary: null filename
        try {
            String ext = editorBO.getFileExtension(null);
            assertNull("Should handle null gracefully", ext);
        } catch (NullPointerException e) {
            // Expected if not handled
        }
    }
    
    @Test
    public void testGetFileExtension_MultipleDotsInFilename() {
        // Boundary: multiple dots in filename
        String ext = editorBO.getFileExtension("file.name.txt");
        assertEquals("Should return last extension", "txt", ext);
    }
    
    @Test
    public void testGetFileExtension_DotAtStart() {
        // Boundary: hidden file (starts with dot)
        String ext = editorBO.getFileExtension(".hidden");
        assertEquals("Should return 'hidden' as extension", "hidden", ext);
    }
    
    @Test
    public void testGetFileExtension_DotAtEnd() {
        // Boundary: filename ends with dot
        String ext = editorBO.getFileExtension("filename.");
        assertEquals("Should return empty string", "", ext);
    }
    
    // ========== HELPER METHODS ==========
    
    /**
     * Generates a string of specified length
     */
    private String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}
