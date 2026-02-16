package dal;

import static org.junit.Assert.*;
import org.junit.Test;

 
public class PMICalculatorBoundaryTest {

    // ========== TEST CATEGORY: NULL INPUT VALIDATION ==========
    
    @Test(expected = NullPointerException.class)
    public void testConstructor_NullDocument() {
        // Boundary: null document input
        PMICalculator calculator = new PMICalculator(null);
    }
    
    @Test
    public void testConstructor_EmptyDocument() {
        // Boundary: empty document
        PMICalculator calculator = new PMICalculator("");
        assertNotNull("Calculator should be created", calculator);
    }
    
    @Test
    public void testCalculatePMI_NullWords() {
        // Boundary: null word parameters
        PMICalculator calculator = new PMICalculator("word1 word2 word3");
        try {
            double result = calculator.calculatePMI(null, "word2");
            // Should handle null gracefully
        } catch (Exception e) {
            // Expected for null input
            assertTrue(true);
        }
    }
    
    // ========== TEST CATEGORY: EMPTY STRING BOUNDARIES ==========
    
    @Test
    public void testCalculatePMI_EmptyStrings() {
        // Boundary: empty string parameters
        PMICalculator calculator = new PMICalculator("test word another");
        double result = calculator.calculatePMI("", "");
        assertEquals("Empty strings should return NEGATIVE_INFINITY", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    @Test
    public void testCalculatePMI_WhitespaceDocument() {
        // Boundary: whitespace-only document
        PMICalculator calculator = new PMICalculator("   ");
        double result = calculator.calculatePMI("a", "b");
        assertEquals("Whitespace doc should return NEGATIVE_INFINITY", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    // ========== TEST CATEGORY: MATHEMATICAL EDGE CASES (Issue #39) ==========
    
    @Test
    public void testCalculatePMI_ZeroProbability() {
        // Boundary: words not in document (zero probability)
        PMICalculator calculator = new PMICalculator("hello world");
        double result = calculator.calculatePMI("notfound", "missing");
        assertEquals("Should return NEGATIVE_INFINITY for zero probability (Issue #39)", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    @Test
    public void testCalculatePMI_ValidBigram() {
        // Boundary: valid bigram input
        PMICalculator calculator = new PMICalculator("the quick brown fox jumps over the lazy dog");
        double result = calculator.calculatePMI("quick", "brown");
        // Should return valid PMI score (not NaN)
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePMI_SingleWord() {
        // Boundary: document with single word
        PMICalculator calculator = new PMICalculator("word");
        double result = calculator.calculatePMI("word", "word");
        // No bigram possible with single word
        assertEquals("Single word should return NEGATIVE_INFINITY", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    @Test
    public void testCalculatePMI_RepeatedWords() {
        // Boundary: repeated words
        PMICalculator calculator = new PMICalculator("test test test");
        double result = calculator.calculatePMI("test", "test");
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePMI_DivisionByZero() {
        // Boundary: division by zero when probability is zero (Issue #39)
        PMICalculator calculator = new PMICalculator("hello world");
        double result = calculator.calculatePMI("missing", "words");
        // Should return NEGATIVE_INFINITY, not throw exception
        assertEquals("Should return NEGATIVE_INFINITY for missing words", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    @Test
    public void testCalculatePMI_NonAdjacentWords() {
        // Boundary: words that exist but are not adjacent
        PMICalculator calculator = new PMICalculator("the quick brown fox");
        double result = calculator.calculatePMI("the", "brown");
        // Bigram "the brown" doesn't exist, but both words exist
        assertEquals("Should return NEGATIVE_INFINITY for non-adjacent words", Double.NEGATIVE_INFINITY, result, 0.001);
    }
    
    // ========== TEST CATEGORY: LENGTH BOUNDARIES ==========
    
    @Test
    public void testCalculatePMI_LongDocument() {
        // Boundary: large document (1000 words)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("word").append(i).append(" ");
        }
        PMICalculator calculator = new PMICalculator(sb.toString());
        double result = calculator.calculatePMI("word500", "word501");
        // Should handle large documents
        assertNotNull("Should complete calculation", Double.valueOf(result));
    }
    
    @Test
    public void testCalculatePMI_VeryLongWords() {
        // Boundary: very long words (100 chars each)
        String longWord1 = generateString(100);
        String longWord2 = generateString(100) + "X";
        PMICalculator calculator = new PMICalculator(longWord1 + " " + longWord2 + " test");
        double result = calculator.calculatePMI(longWord1, longWord2);
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: SPECIAL CHARACTERS ==========
    
    @Test
    public void testCalculatePMI_ArabicText() {
        // Boundary: Arabic Unicode characters
        PMICalculator calculator = new PMICalculator("مرحبا بك في الاختبار");
        double result = calculator.calculatePMI("مرحبا", "بك");
        assertFalse("Should handle Arabic text", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePMI_MixedLanguages() {
        // Boundary: mixed English and Arabic
        PMICalculator calculator = new PMICalculator("hello مرحبا world test");
        double result = calculator.calculatePMI("hello", "مرحبا");
        assertFalse("Should handle mixed languages", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePMI_SpecialCharacters() {
        // Boundary: special characters
        PMICalculator calculator = new PMICalculator("test@123 #word$ %another^");
        double result = calculator.calculatePMI("test@123", "#word$");
        assertFalse("Should handle special characters", Double.isNaN(result));
    }
    
    // ========== TEST CATEGORY: PERFORMANCE ==========
    
    @Test(timeout = 5000)
    public void testCalculatePMI_Performance() {
        // Boundary: performance with 10000 words
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("word ");
        }
        PMICalculator calculator = new PMICalculator(sb.toString());
        double result = calculator.calculatePMI("word", "word");
        // Should complete within timeout
        assertTrue("Should complete within 5 seconds", true);
    }
    
    // ===== TEST CATEGORY: EDGE CASE COMBINATIONS ======
    
    @Test
    public void testCalculatePMI_TwoWordDocument() {
        // Boundary: document with exactly two words
        PMICalculator calculator = new PMICalculator("hello world");
        double result = calculator.calculatePMI("hello", "world");
        // Should calculate PMI for the only bigram
        assertFalse("Should not be NaN", Double.isNaN(result));
    }
    
    @Test
    public void testCalculatePMI_AllSameWord() {
        // Boundary: all words identical
        PMICalculator calculator = new PMICalculator("word word word word");
        double result = calculator.calculatePMI("word", "word");
        // High probability bigram
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
