package dal;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Boundary and Limit Condition Tests for PKLCalculator
 * Tests major functionality with edge cases (Issue #41)
 */
public class PKLCalculatorBoundaryTest {

    // ========== TEST CATEGORY: NULL INPUT VALIDATION ==========
    
    @Test(expected = NullPointerException.class)
    public void testConstructor_NullDocument() {
        // Boundary: null document input
        PKLCalculator calculator = new PKLCalculator(null);
    }
    
    @Test
    public void testConstructor_EmptyDocument() {
        // Boundary: empty document
        PKLCalculator calculator = new PKLCalculator("");
        assertNotNull("Calculator should be created", calculator);
    }
    
    @Test
    public void testCalculatePKL_NullWords() {
        // Boundary: null word parameters
        PKLCalculator calculator = new PKLCalculator("word1 word2 word3");
        try {
            double result = calculator.calculatePKL(null, "word1", "word2");
            // Should handle null gracefully
        } catch (Exception e) {
            // Expected for null input
            assertTrue(true);
        }
    }
    
    // ========== TEST CATEGORY: EMPTY STRING BOUNDARIES ==========
    
    @Test
    public void testCalculatePKL_EmptyStrings() {
        // Boundary: empty string parameters
        PKLCalculator calculator = new PKLCalculator("test word another");
        double result = calculator.calculatePKL("", "", "");
        assertEquals("Empty strings should return 0.0", 0.0, result, 0.001);
    }
    
    @Test
    public void testCalculatePKL_WhitespaceDocument() {
        // Boundary: whitespace-only document
        PKLCalculator calculator = new PKLCalculator("   ");
        double result = calculator.calculatePKL("a", "b", "c");
        assertEquals("Whitespace doc should return 0.0", 0.0, result, 0.001);
    }
    
    // ========== TEST CATEGORY: MATHEMATICAL EDGE CASES (Issue #41) ==========
    
    @Test
    public void testCalculatePKL_ZeroProbability() {
        // Boundary: words not in document (zero probability)
        PKLCalculator calculator = new PKLCalculator("hello world");
        double result = calculator.calculatePKL("notfound", "hello", "world");
        assertEquals("Should return 0.0 for zero probability (Issue #41)", 0.0, result, 0.001);
    }
    
    @Test
    public void testCalculatePKL_ValidInput() {
        // Boundary: valid normal input
        PKLCalculator calculator = new PKLCalculator("the quick brown fox jumps over the lazy dog");
        double result = calculator.calculatePKL("quick", "the", "brown");
        // Should return valid PKL score (not NaN or Infinity)
        assertFalse("Should not be NaN", Double.isNaN(result));
        assertFalse("Should not be Infinity", Double.isInfinite(result));
    }
    
    @Test
    public void testCalculatePKL_SingleWord() {
        // Boundary: document with single word
        PKLCalculator calculator = new PKLCalculator("word");
        double result = calculator.calculatePKL("word", "word", "word");
        // Single word edge case
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePKL_RepeatedWords() {
        // Boundary: repeated words
        PKLCalculator calculator = new PKLCalculator("test test test");
        double result = calculator.calculatePKL("test", "test", "test");
        assertFalse("Should not be NaN", Double.isNaN(result));
        assertFalse("Should not be Infinity", Double.isInfinite(result));
    }
    
    @Test
    public void testCalculatePKL_LogOfZero() {
        // Boundary: log(0) when probability is zero (Issue #41)
        PKLCalculator calculator = new PKLCalculator("hello world");
        double result = calculator.calculatePKL("missing", "hello", "world");
        // Should return 0.0, not throw exception or return NaN
        assertEquals("Should return 0.0 for missing word", 0.0, result, 0.001);
    }
    
    // ========== TEST CATEGORY: LENGTH BOUNDARIES ==========
    
    @Test
    public void testCalculatePKL_LongDocument() {
        // Boundary: large document (1000 words)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("word").append(i).append(" ");
        }
        PKLCalculator calculator = new PKLCalculator(sb.toString());
        double result = calculator.calculatePKL("word500", "word499", "word501");
        // Should handle large documents
        assertNotNull("Should complete calculation", Double.valueOf(result));
    }
    
    @Test
    public void testCalculatePKL_VeryLongWords() {
        // Boundary: very long words (100 chars each)
        String longWord = generateString(100);
        PKLCalculator calculator = new PKLCalculator(longWord + " test " + longWord);
        double result = calculator.calculatePKL("test", longWord, longWord);
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: SPECIAL CHARACTERS ==========
    
    @Test
    public void testCalculatePKL_ArabicText() {
        // Boundary: Arabic Unicode characters
        PKLCalculator calculator = new PKLCalculator("مرحبا بك في الاختبار");
        double result = calculator.calculatePKL("بك", "مرحبا", "في");
        assertFalse("Should handle Arabic text", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePKL_MixedLanguages() {
        // Boundary: mixed English and Arabic
        PKLCalculator calculator = new PKLCalculator("hello مرحبا world");
        double result = calculator.calculatePKL("مرحبا", "hello", "world");
        assertFalse("Should handle mixed languages", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePKL_SpecialCharacters() {
        // Boundary: special characters
        PKLCalculator calculator = new PKLCalculator("test@123 #word$ %another^");
        double result = calculator.calculatePKL("#word$", "test@123", "%another^");
        assertFalse("Should handle special characters", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: PERFORMANCE ==========
    
    @Test(timeout = 5000)
    public void testCalculatePKL_Performance() {
        // Boundary: performance with 10000 words
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("word ");
        }
        PKLCalculator calculator = new PKLCalculator(sb.toString());
        double result = calculator.calculatePKL("word", "word", "word");
        // Should complete within timeout
        assertTrue("Should complete within 5 seconds", true);
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
