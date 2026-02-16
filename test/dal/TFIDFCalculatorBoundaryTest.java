package dal;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Boundary and Limit Condition Tests for TFIDFCalculator
 * Tests major functionality with edge cases (Issue #31, #37)
 */
public class TFIDFCalculatorBoundaryTest {

    private TFIDFCalculator calculator;

    @Before
    public void setUp() {
        calculator = new TFIDFCalculator();
    }

    // ========== TEST CATEGORY: NULL INPUT VALIDATION ==========
    
    @Test(expected = NullPointerException.class)
    public void testCalculateTFIDF_NullDocument() {
        // Boundary: null document input
        calculator.calculateDocumentTfIdf(null);
    }
    
    @Test
    public void testAddDocument_Null() {
        // Boundary: adding null document to corpus
        try {
            calculator.addDocumentToCorpus(null);
            fail("Should throw exception for null document");
        } catch (Exception e) {
            // Expected
            assertTrue(true);
        }
    }
    
    // ========== TEST CATEGORY: EMPTY STRING BOUNDARIES ==========
    
    @Test
    public void testCalculateTFIDF_EmptyDocument() {
        // Boundary: empty document
        calculator.addDocumentToCorpus("test document one");
        double result = calculator.calculateDocumentTfIdf("");
        // Should handle empty document gracefully
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_WhitespaceDocument() {
        // Boundary: whitespace-only document
        calculator.addDocumentToCorpus("test document");
        double result = calculator.calculateDocumentTfIdf("   \t\n  ");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_EmptyCorpus() {
        // Boundary: calculating TF-IDF without corpus
        double result = calculator.calculateDocumentTfIdf("test document");
        // Should handle empty corpus
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: MATHEMATICAL EDGE CASES (Issue #31, #37) ==========
    
    @Test
    public void testCalculateTFIDF_DivisionByZero() {
        // Boundary: division by zero when total words is zero (Issue #31)
        calculator.addDocumentToCorpus("test");
        double result = calculator.calculateDocumentTfIdf("!@#$%"); // No words after preprocessing
        // Should not throw exception, should return 0 or handle gracefully
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_LogOfZero() {
        // Boundary: log(0) when word doesn't appear (Issue #37)
        calculator.addDocumentToCorpus("hello world");
        double result = calculator.calculateDocumentTfIdf("test document");
        // Should handle log(0) gracefully
        assertFalse("Should not be NaN", Double.isNaN(result));
        assertFalse("Should not be Infinity", Double.isInfinite(result));
    }
    
    @Test
    public void testCalculateTFIDF_SingleWord() {
        // Boundary: document with single word
        calculator.addDocumentToCorpus("word");
        double result = calculator.calculateDocumentTfIdf("word");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_AllSameWord() {
        // Boundary: all words identical
        calculator.addDocumentToCorpus("word word word");
        double result = calculator.calculateDocumentTfIdf("word word word");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_ValidInput() {
        // Boundary: normal valid input
        calculator.addDocumentToCorpus("the quick brown fox");
        calculator.addDocumentToCorpus("the lazy dog");
        double result = calculator.calculateDocumentTfIdf("the quick fox");
        // Should return valid TF-IDF score
        assertFalse("Should not be NaN", Double.isNaN(result));
        assertFalse("Should not be Infinity", Double.isInfinite(result));
        assertTrue("Should be non-negative", result >= 0);
    }
    
    // ========== TEST CATEGORY: LENGTH BOUNDARIES ==========
    
    @Test
    public void testCalculateTFIDF_LargeCorpus() {
        // Boundary: large corpus (100 documents)
        for (int i = 0; i < 100; i++) {
            calculator.addDocumentToCorpus("document " + i + " with content");
        }
        double result = calculator.calculateDocumentTfIdf("test document");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_LongDocument() {
        // Boundary: very long document (1000 words)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("word").append(i).append(" ");
        }
        calculator.addDocumentToCorpus("test");
        double result = calculator.calculateDocumentTfIdf(sb.toString());
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_VeryLongWords() {
        // Boundary: very long words (100 chars each)
        String longWord = generateString(100);
        calculator.addDocumentToCorpus(longWord + " test");
        double result = calculator.calculateDocumentTfIdf(longWord);
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: SPECIAL CHARACTERS ==========
    
    @Test
    public void testCalculateTFIDF_ArabicText() {
        // Boundary: Arabic Unicode characters
        calculator.addDocumentToCorpus("مرحبا بك في الاختبار");
        double result = calculator.calculateDocumentTfIdf("الاختبار العربي");
        assertFalse("Should handle Arabic text", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_MixedLanguages() {
        // Boundary: mixed English and Arabic
        calculator.addDocumentToCorpus("hello مرحبا world");
        double result = calculator.calculateDocumentTfIdf("test مرحبا");
        assertFalse("Should handle mixed languages", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_SpecialCharacters() {
        // Boundary: special characters
        calculator.addDocumentToCorpus("test@123 #word$");
        double result = calculator.calculateDocumentTfIdf("test@123");
        assertFalse("Should handle special characters", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: PERFORMANCE ==========
    
    @Test(timeout = 10000)
    public void testCalculateTFIDF_Performance() {
        // Boundary: performance test with large corpus and document
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 100; j++) {
                sb.append("word").append(j).append(" ");
            }
            calculator.addDocumentToCorpus(sb.toString());
        }
        
        StringBuilder testDoc = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            testDoc.append("test").append(i).append(" ");
        }
        
        double result = calculator.calculateDocumentTfIdf(testDoc.toString());
        // Should complete within timeout
        assertTrue("Should complete within 10 seconds", true);
    }
    
    // ========== TEST CATEGORY: EDGE CASE COMBINATIONS ==========
    
    @Test
    public void testCalculateTFIDF_RepeatedDocuments() {
        // Boundary: adding same document multiple times
        calculator.addDocumentToCorpus("test document");
        calculator.addDocumentToCorpus("test document");
        calculator.addDocumentToCorpus("test document");
        double result = calculator.calculateDocumentTfIdf("test");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_NoCommonWords() {
        // Boundary: corpus and document have no common words
        calculator.addDocumentToCorpus("hello world");
        double result = calculator.calculateDocumentTfIdf("completely different text");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculateTFIDF_AllCommonWords() {
        // Boundary: all words appear in corpus
        calculator.addDocumentToCorpus("the quick brown fox jumps over lazy dog");
        double result = calculator.calculateDocumentTfIdf("the fox jumps");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    // ========== HELPER METHODS ==========
    
    private String generateString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}
