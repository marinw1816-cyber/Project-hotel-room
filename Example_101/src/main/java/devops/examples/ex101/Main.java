package devops.examples.ex101;
import java.util.Arrays;
/**
 * Command-line entry point.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(
                    "Usage: java -jar vowel-counter-1.0.0.jar \"your tex\"");
            System.exit(1);
        }

        int vowelCount =0;


        for(String text : args) {

            vowelCount+=VowelCounter.countVowels(text);

        }
        System.out.println("Input: " + Arrays.toString(args));
        System.out.println("Number of vowels: " + vowelCount);
    }
}
