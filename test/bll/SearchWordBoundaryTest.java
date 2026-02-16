package bll;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dto.Documents;
import dto.Pages;

/**
 * Boundary and Limit Condition Tests for SearchWord (Business Logic Layer)
 * Tests: Minimum keyword length, empty input, SQL injection attempts, case sensitivity
 */
public class SearchWordBoundaryTest {
    
    private List<Documents> testDocuments;
    
    @Before
    public void setUp() {
        testDocuments = new ArrayList<>();
        
        // Create test document 1
        Documents doc1 = new Documents(1, "TestFile1.txt", "hash1", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc1.setId(1);
        doc1.setName("TestFile1.txt");
        
        Pages page1 = new Pages(1, 1, 1, "This is a test content with keyword search");
        page1.setPageNumber(1);
        page1.setPageContent("This is a test content with keyword search");
        
        List<Pages> pages1 = new ArrayList<>();
        pages1.add(page1);
        doc1.setPages(pages1);
        testDocuments.add(doc1);
        
        // Create test document 2
        Documents doc2 = new Documents(2, "TestFile2.txt", "hash2", "2024-01-02", "2024-01-02", new ArrayList<>());
        doc2.setId(2);
        doc2.setName("TestFile2.txt");
        
        Pages page2 = new Pages(1, 2, 1, "Another document for testing purposes");
        page2.setPageNumber(1);
        page2.setPageContent("Another document for testing purposes");
        
        List<Pages> pages2 = new ArrayList<>();
        pages2.add(page2);
        doc2.setPages(pages2);
        testDocuments.add(doc2);
    }
    
    // ===== TEST CATEGORY: MINIMUM KEYWORD LENGTH (Issue #23) ======
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_EmptyString() {
        // Boundary: empty keyword (Issue #23)
        SearchWord.searchKeyword("", testDocuments);
        fail("Should throw IllegalArgumentException for empty keyword");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_OneCharacter() {
        // Boundary: 1 character keyword (Issue #23)
        SearchWord.searchKeyword("a", testDocuments);
        fail("Should throw IllegalArgumentException for 1-char keyword");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_TwoCharacters() {
        // Boundary: 2 characters keyword (Issue #23)
        SearchWord.searchKeyword("ab", testDocuments);
        fail("Should throw IllegalArgumentException for 2-char keyword");
    }
    
    @Test
    public void testSearchKeyword_ThreeCharacters() {
        // Boundary: minimum valid length (3 characters)
        List<String> results = SearchWord.searchKeyword("tes", testDocuments);
        assertNotNull("Should return results for 3-char keyword", results);
        assertTrue("Should find matches", results.size() > 0);
    }
    
    @Test
    public void testSearchKeyword_ExactlyThreeCharacters() {
        // Boundary: exactly 3 characters (edge case)
        List<String> results = SearchWord.searchKeyword("for", testDocuments);
        assertNotNull("Should handle 3-char keyword", results);
    }
    
    // ========== TEST CATEGORY: NULL INPUT VALIDATION ==========
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_NullKeyword() {
        // Boundary: null keyword
        try {
            SearchWord.searchKeyword(null, testDocuments);
            fail("Should throw exception for null keyword");
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Expected exception for null");
        }
    }
    
    @Test
    public void testSearchKeyword_NullDocumentList() {
        // Boundary: null document list
        try {
            List<String> results = SearchWord.searchKeyword("test", null);
            fail("Should throw NullPointerException for null document list");
        } catch (NullPointerException e) {
            // Expected
        }
    }
    
    @Test
    public void testSearchKeyword_EmptyDocumentList() {
        // Boundary: empty document list
        List<String> results = SearchWord.searchKeyword("test", new ArrayList<>());
        assertNotNull("Should return empty list", results);
        assertEquals("Should have no results", 0, results.size());
    }
    
    // ========== TEST CATEGORY: WHITESPACE HANDLING ==========
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_WhitespaceOnly() {
        // Boundary: whitespace-only keyword
        SearchWord.searchKeyword("   ", testDocuments);
        fail("Should reject whitespace-only keyword");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSearchKeyword_TabsAndSpaces() {
        // Boundary: tabs and spaces
        SearchWord.searchKeyword("\t\n", testDocuments);
        fail("Should reject tabs/newlines as keyword");
    }
    
    @Test
    public void testSearchKeyword_LeadingTrailingSpaces() {
        // Boundary: keyword with spaces (length check on trimmed?)
        try {
            List<String> results = SearchWord.searchKeyword("  test  ", testDocuments);
            // If it works, spaces are included in length
            assertNotNull("Should handle spaces", results);
        } catch (IllegalArgumentException e) {
            // If it fails, spaces are trimmed first
        }
    }
    
    // ======= TEST CATEGORY: SQL INJECTION ATTEMPTS (Issue #22) =====
    
    @Test
    public void testSearchKeyword_SQLInjectionSingleQuote() {
        // Boundary: SQL injection with single quote
        String sqlInjection = "' OR '1'='1";
        try {
            List<String> results = SearchWord.searchKeyword(sqlInjection, testDocuments);
            // Should not find anything (not actual SQL)
            assertNotNull("Should handle SQL injection attempt", results);
        } catch (Exception e) {
            // Should not throw exception
            fail("Should not allow SQL injection: " + e.getMessage());
        }
    }
    
    @Test
    public void testSearchKeyword_SQLInjectionDropTable() {
        // Boundary: SQL injection DROP TABLE
        String sqlInjection = "'; DROP TABLE files; --";
        try {
            List<String> results = SearchWord.searchKeyword(sqlInjection, testDocuments);
            assertNotNull("Should handle DROP TABLE attempt", results);
        } catch (Exception e) {
            fail("Should not execute SQL: " + e.getMessage());
        }
    }
    
    @Test
    public void testSearchKeyword_SQLInjectionUnion() {
        // Boundary: SQL injection UNION
        String sqlInjection = "test' UNION SELECT * FROM users --";
        try {
            List<String> results = SearchWord.searchKeyword(sqlInjection, testDocuments);
            assertNotNull("Should handle UNION injection", results);
        } catch (Exception e) {
            fail("Should not execute SQL: " + e.getMessage());
        }
    }
    
    @Test
    public void testSearchKeyword_SpecialCharacters() {
        // Boundary: special characters
        String special = "<script>alert('xss')</script>";
        try {
            List<String> results = SearchWord.searchKeyword(special, testDocuments);
            assertNotNull("Should handle special characters", results);
        } catch (IllegalArgumentException e) {
            // Expected - too short after filtering
        }
    }
    
    // ========== TEST CATEGORY: CASE SENSITIVITY (Issue #24) ==========
    
    @Test
    public void testSearchKeyword_CaseInsensitive() {
        // Boundary: case insensitivity
        List<String> resultsLower = SearchWord.searchKeyword("test", testDocuments);
        List<String> resultsUpper = SearchWord.searchKeyword("TEST", testDocuments);
        List<String> resultsMixed = SearchWord.searchKeyword("TeSt", testDocuments);
        
        assertTrue("Should find lowercase", resultsLower.size() > 0);
        assertTrue("Should find uppercase", resultsUpper.size() > 0);
        assertTrue("Should find mixed case", resultsMixed.size() > 0);
        
        // All should return same count if truly case-insensitive
        assertEquals("Case should not matter", resultsLower.size(), resultsUpper.size());
    }
    
    // ========== TEST CATEGORY: SEARCH RESULT BOUNDARIES ==========
    
    @Test
    public void testSearchKeyword_NoMatches() {
        // Boundary: keyword not found
        List<String> results = SearchWord.searchKeyword("xyz123notfound", testDocuments);
        assertNotNull("Should return empty list for no matches", results);
        assertEquals("Should have 0 results", 0, results.size());
    }
    
    @Test
    public void testSearchKeyword_MultipleMatches() {
        // Boundary: keyword appears multiple times
        Documents doc = new Documents(1, "MultiMatch.txt", "hash", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc.setName("MultiMatch.txt");
        Pages page = new Pages(1, 1, 1, "test test test keyword test");
        page.setPageContent("test test test keyword test");
        List<Pages> pages = new ArrayList<>();
        pages.add(page);
        doc.setPages(pages);
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        List<String> results = SearchWord.searchKeyword("test", docs);
        // Should only return one match per page (breaks after first match)
        assertTrue("Should find at least one match", results.size() >= 1);
    }
    
    @Test
    public void testSearchKeyword_KeywordAtStart() {
        // Boundary: keyword at start of content (no prefix word)
        Documents doc = new Documents(1, "StartKeyword.txt", "hash", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc.setName("StartKeyword.txt");
        Pages page = new Pages(1, 1, 1, "keyword is at the start");
        page.setPageContent("keyword is at the start");
        List<Pages> pages = new ArrayList<>();
        pages.add(page);
        doc.setPages(pages);
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        List<String> results = SearchWord.searchKeyword("keyword", docs);
        assertTrue("Should handle keyword at start", results.size() > 0);
        assertTrue("Should have empty prefix", results.get(0).contains(" -  keyword"));
    }
    
    @Test
    public void testSearchKeyword_KeywordWithPrefix() {
        // Boundary: keyword with prefix word
        List<String> results = SearchWord.searchKeyword("test", testDocuments);
        assertTrue("Should find keyword with prefix", results.size() > 0);
        // Result should contain prefix word
        assertTrue("Should include prefix word in result", 
            results.get(0).contains("a test") || results.get(0).contains("is test"));
    }
    
    // ========== TEST CATEGORY: LARGE DATA BOUNDARIES ==========
    
    @Test
    public void testSearchKeyword_VeryLongKeyword() {
        // Boundary: very long keyword (100+ characters)
        StringBuilder longKeyword = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longKeyword.append("a");
        }
        
        List<String> results = SearchWord.searchKeyword(longKeyword.toString(), testDocuments);
        assertNotNull("Should handle long keyword", results);
        assertEquals("Should not find match", 0, results.size());
    }
    
    @Test
    public void testSearchKeyword_ManyDocuments() {
        // Boundary: large number of documents (1000)
        List<Documents> manyDocs = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Documents doc = new Documents(i, "Doc" + i + ".txt", "hash" + i, "2024-01-01", "2024-01-01", new ArrayList<>());
            doc.setName("Doc" + i + ".txt");
            Pages page = new Pages(1, i, 1, "Content number " + i + " for testing");
            page.setPageContent("Content number " + i + " for testing");
            List<Pages> pages = new ArrayList<>();
            pages.add(page);
            doc.setPages(pages);
            manyDocs.add(doc);
        }
        
        long startTime = System.currentTimeMillis();
        List<String> results = SearchWord.searchKeyword("testing", manyDocs);
        long endTime = System.currentTimeMillis();
        
        assertNotNull("Should handle many documents", results);
        assertTrue("Should find matches in many documents", results.size() > 0);
        assertTrue("Should complete in reasonable time (<5s)", (endTime - startTime) < 5000);
    }
    
    @Test
    public void testSearchKeyword_DocumentWithoutPages() {
        // Boundary: document with no pages
        Documents doc = new Documents(1, "NoPages.txt", "hash", "2024-01-01", "2024-01-01", new ArrayList<>());
        doc.setName("NoPages.txt");
        doc.setPages(new ArrayList<>()); // Empty pages list
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        List<String> results = SearchWord.searchKeyword("test", docs);
        assertNotNull("Should handle document without pages", results);
        assertEquals("Should return no results", 0, results.size());
    }
    
    @Test
    public void testSearchKeyword_DocumentWithNullPages() {
        // Boundary: document with null pages
        Documents doc = new Documents(1, "NullPages.txt", "hash", "2024-01-01", "2024-01-01", null);
        doc.setName("NullPages.txt");
        doc.setPages(null);
        
        List<Documents> docs = new ArrayList<>();
        docs.add(doc);
        
        try {
            List<String> results = SearchWord.searchKeyword("test", docs);
            assertNotNull("Should handle null pages", results);
        } catch (NullPointerException e) {
            
        }
    }
}
