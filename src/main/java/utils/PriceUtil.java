package utils;

public class PriceUtil {

    /** "Rs. 1,500" -> 1500 */
    public static int parse(String text) {
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }
}