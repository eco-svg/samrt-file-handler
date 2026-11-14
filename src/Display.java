/*
 * Display.java
 * All console "drawing" lives here: lines, titles, patterns, bar charts
 * and helpers that format text for tables.
 *
 * Syllabus topics shown in this file:
 *   - Constants (final), data types
 *   - Method overloading (line, formatSize)
 *   - Nested loops and pattern programming (triangle, pyramid, diamond)
 *   - String methods, StringBuilder
 */
public class Display {

    // Constants: values that never change while the program runs.
    public static final int WIDTH = 64;
    private static final String[] UNITS = { "B", "KB", "MB", "GB", "TB" };

    // ---------------------------------------------------------------
    // Method overloading: three methods share the name "line" but take
    // different parameters. Java picks one by looking at the arguments.
    // ---------------------------------------------------------------
    public static void line() {
        line('-', WIDTH);
    }

    public static void line(char ch) {
        line(ch, WIDTH);
    }

    public static void line(char ch, int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(ch);
        }
        System.out.println();
    }

    public static void title(String text) {
        System.out.println();
        line('=');
        // Centre the text: spaces on the left = (width - text length) / 2
        int left = (WIDTH - text.length()) / 2;
        for (int i = 0; i < left; i++) {
            System.out.print(' ');
        }
        System.out.println(text);
        line('=');
    }

    // ---------------------------------------------------------------
    // Pattern programming
    // ---------------------------------------------------------------

    /* Inverted right-angled triangle, used as the heading of the
     * "largest files" ranking:
     *   * * *
     *   * *
     *   *
     */
    public static void triangle(int rows, int indent) {
        for (int i = rows; i >= 1; i--) {          // one pass per row
            for (int s = 0; s < indent; s++) {
                System.out.print(' ');
            }
            for (int j = 1; j <= i; j++) {         // row has i stars
                System.out.print("* ");
            }
            System.out.println();
        }
    }

    /* Pyramid:
     *     *
     *    ***
     *   *****
     * Row i has (rows - i) spaces and (2*i - 1) stars.
     */
    public static void pyramid(int rows, int indent) {
        for (int i = 1; i <= rows; i++) {
            for (int s = 0; s < indent + rows - i; s++) {
                System.out.print(' ');
            }
            for (int j = 1; j <= 2 * i - 1; j++) {
                System.out.print('*');
            }
            System.out.println();
        }
    }

    /* Diamond = a pyramid followed by an upside-down pyramid. */
    public static void diamond(int rows, int indent) {
        pyramid(rows, indent);
        for (int i = rows - 1; i >= 1; i--) {
            for (int s = 0; s < indent + rows - i; s++) {
                System.out.print(' ');
            }
            for (int j = 1; j <= 2 * i - 1; j++) {
                System.out.print('*');
            }
            System.out.println();
        }
    }

    /* Returns a horizontal bar such as "#########" for the bar chart. */
    public static String bar(int length, char ch) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------
    // Size formatting (overloaded)
    // ---------------------------------------------------------------
    public static String formatSize(long bytes) {
        return formatSize(bytes, 1);
    }

    public static String formatSize(long bytes, int decimals) {
        double value = bytes;          // widening: long -> double
        int unit = 0;
        while (value >= 1024 && unit < UNITS.length - 1) {
            value = value / 1024;
            unit++;
        }
        if (unit == 0) {
            return bytes + " B";       // whole bytes need no decimals
        }
        return String.format("%." + decimals + "f %s", value, UNITS[unit]);
    }

    /*
     * Makes a string exactly "width" characters long so table columns
     * line up. Long text is cut with substring(), short text is padded.
     *
     * Strings are immutable: substring() and concat() never change the
     * original, they return a NEW string. That is why we must use the
     * returned value.
     */
    public static String fit(String text, int width) {
        if (text.length() > width) {
            String cut = text.substring(0, width - 2);
            return cut.concat("..");
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
