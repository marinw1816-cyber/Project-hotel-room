package devops.examples.ex101;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
class VowelCounterTest {
    @Test
    void countsLowercaseVowels() {
        assertEquals(5, VowelCounter.countVowels("aeiou"));
    }
    @Test
    void countsUppercaseVowels() {
        assertEquals(5, VowelCounter.countVowels("AEIOU"));
    }
    @Test
    void countsMixedCaseVowels() {
        assertEquals(10, VowelCounter.countVowels("AaEeIiOoUu"));
    }
    @Test
    void ignoresConsonants() {
        assertEquals(0, VowelCounter.countVowels("bcdfghjklmnpqrstvwxyz"));
    }
    @Test
    void countsVowelsInsideWords() {
        assertEquals(2, VowelCounter.countVowels("Hello"));
    }
    @Test
    void ignoresSpacesDigitsAndPunctuation() {
        assertEquals(4, VowelCounter.countVowels("Java 17, Maven! 2026"));
    }
    @Test
    void doesNotCountY() {
        assertEquals(0, VowelCounter.countVowels("rhythm"));
    }
    @Test
    void emptyStringHasZeroVowels() {
        assertEquals(0, VowelCounter.countVowels(""));
    }
    @Test
    void nullInputIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> VowelCounter.countVowels(null));
    }
    @Test
    void countsVowelsInWonderful() {
        assertEquals(3, VowelCounter.countVowels("Wonderful"));
    }}
