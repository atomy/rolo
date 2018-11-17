package de.iogames.rolo;

/**
 * Helping methods for strings.
 */
public class StringHelper {
    /**
     * Return length of a String using utf-8 encoding
     *
     * @param sequence CharSequence
     * @return int
     */
    public static int utf8Length(CharSequence sequence) {
        int count = 0;
        for (int i = 0, len = sequence.length(); i < len; i++) {
            char ch = sequence.charAt(i);
            if (ch <= 0x7F) {
                count++;
            } else if (ch <= 0x7FF) {
                count += 2;
            } else if (Character.isHighSurrogate(ch)) {
                count += 4;
                ++i;
            } else {
                count += 3;
            }
        }
        return count;
    }
}
