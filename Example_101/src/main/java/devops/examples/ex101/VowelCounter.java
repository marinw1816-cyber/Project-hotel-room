package devops.examples.ex101;
public final class VowelCounter {
    public VowelCounter() {}
    public static int countVowels(String text ) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Input string cannot be null.");
        }
        int count = 0;

        for (char character : text.toCharArray()) {
            switch (Character.toLowerCase(character)) {
                case 'a':
                case 'e':
                case 'i':
                case 'o':
                case 'u':
                    count++;
                    break;
                default:

            }
        }

        return count;
    }
}
