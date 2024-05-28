package ru.ac.uniyar.katkov.nonograms.utils;

import java.math.BigInteger;

/**
 * colorises your text using ANSI
 *
 * @apiNote Most terminals would work with this, some terminals won't
 *
 * @author cindrmon
 */
public class RGBToANSIConverter {

    public static final String CLEAR = "\u001B[0m";

    /**
     *
     * selects a color using rgb/hex and converts to ANSI code
     *
     * @param r - Red value (in decimal)
     * @param g - Green value (in decimal)
     * @param b - Blue value (in decimal)
     * @return escape string with set rgb value
     */
    private static String selectColor(int r, int g, int b) {
        if (r <= 255 && g <= 255 && b <= 255 && r >= 0 && g >= 0 && b >= 0)
            return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
        else
            return "\u001B[38;2;255;255;255m";
    }

    /**
     *
     * selects a color using rgb/hex and converts to ANSI code
     *
     * @param hexValue - color in hex value (don't include the '#')
     * @return escape string with set rgb value (defaults to white if invalid hex
     *         value)
     */
    public static String selectColor(String hexValue) {
        String rHex = "";
        String gHex = "";
        String bHex = "";
        if (hexValue.length() > 6 || !hexValue.matches("[0-9a-fA-F]{6}$"))
            return "\u001B[38;2;255;255;255m";
        else {
            rHex = hexValue.substring(0, 2).toUpperCase();
            gHex = hexValue.substring(2, 4).toUpperCase();
            bHex = hexValue.substring(4, 6).toUpperCase();
        }
        // Convert to decimal
        BigInteger r = new BigInteger(rHex, 16);
        BigInteger g = new BigInteger(gHex, 16);
        BigInteger b = new BigInteger(bHex, 16);

        return "\u001B[48;2;" + r + ";" + g + ";" + b + "m";

    }

    /**
     *
     * colorise text using ANSI characters
     *
     * @apiNote This does not work with the eclipse console. You must use an
     *          external console for it to work.
     *
     * @param text - Text to colorise
     * @param r    - Red value (in decimal)
     * @param g    - Green value (in decimal)
     * @param b    - Blue value (in decimal)
     * @return colorised string
     */
    public static String colorise(String text, int r, int g, int b) {
        String selectedColor = selectColor(r, g, b);
        return selectedColor + text + CLEAR;
    }

    /**
     *
     * colorise text using ANSI characters
     *
     * @apiNote This does not work with the eclipse console. You must use an
     *          external console for it to work.
     *
     * @param text  - Text to colorise
     * @param hexValue - hex value (without the '#')
     * @return colorised string
     */
    public static String colorise(String text, String hexValue) {
        String selectedColor = "";

        // if it is a hex value
        if (hexValue.matches("[0-9a-f]{6}$"))
            selectedColor = selectColor(hexValue);

            // if it is an invalid hex value
        else
            selectedColor = CLEAR;

        return selectedColor + text + CLEAR;
    }

}