import java.io.File;
import java.util.Scanner;

/*
 * ================================================================
 *  SMART FILE ORGANIZER AND DUPLICATE FINDER
 *  Introduction to Programming (Java) - course project
 * ================================================================
 * Main.java
 * Program entry point: shows the menu, reads the user's choice with
 * Scanner and calls the right method.
 *
 * Syllabus topics shown in this file:
 *   - Scanner input, System.out output (print, println, printf)
 *   - Data types, variables, constants
 *   - Arithmetic, relational and logical operators
 *   - if / else, nested if, switch
 *   - for, while, do-while, break, continue
 *   - Series (sum of natural numbers, sum of file sizes)
 */
public class Main {

    private static final String DEMO_FOLDER = "demo_messy";
    private static final int MAX_BAR = 30;      // longest bar in the chart
    private static final int TOP_FILES = 5;

    private static final Scanner input = new Scanner(System.in);
    private static boolean lastScanRecursive = true;

    public static void main(String[] args) {
        Display.pyramid(4, 28);
        Display.title("SMART FILE ORGANIZER & DUPLICATE FINDER");

        int choice;
        // do-while: the menu must be shown at least once.
        do {
            printMenu();
            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    scanFolder();
                    break;
                case 2:
                    if (isScanned()) showFiles();
                    break;
                case 3:
                    if (isScanned()) showStatistics();
                    break;
                case 4:
                    if (isScanned()) searchFiles();
                    break;
                case 5:
                    if (isScanned()) excludeFile();
                    break;
                case 6:
                    if (isScanned()) findDuplicates();
                    break;
                case 7:
                    if (isScanned()) organizeFiles();
                    break;
                case 8:
                    undoLastMove();
                    break;
                case 9:
                    if (isScanned()) showTree();
                    break;
                case 10:
                    createDemoFolder();
                    break;
                case 0:
                    System.out.println();
                    Display.diamond(3, 29);
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 0 to 10.");
            }
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println();
        Display.line();
        if (FileStore.root == null) {
            System.out.println(" Folder: (none scanned yet)");
        } else {
            System.out.println(" Folder: " + FileStore.root.getPath()
                    + "   [" + FileStore.count + " files]");
        }
        Display.line();
        System.out.println("  1. Scan a folder");
        System.out.println("  2. Show file list");
        System.out.println("  3. Statistics report");
        System.out.println("  4. Search file by name");
        System.out.println("  5. Exclude a file from the list");
        System.out.println("  6. Find duplicate files");
        System.out.println("  7. Organize files by type");
        System.out.println("  8. Undo last move");
        System.out.println("  9. Folder tree (recursion)");
        System.out.println(" 10. Create demo folder for testing");
        System.out.println("  0. Exit");
        Display.line();
    }

    // ---------------------------------------------------------------
    // Input helpers
    // ---------------------------------------------------------------

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) {       // input closed (Ctrl+D)
            System.out.println();
            System.exit(0);
        }
        return input.nextLine().trim();
    }

    /*
     * Reads a whole number, checking each character with charAt()
     * instead of letting the program crash on bad input.
     * Returns -1 when the text is not a valid number.
     */
    private static int readInt(String prompt) {
        String text = readLine(prompt);
        if (text.length() == 0 || text.length() > 9) {
            return -1;
        }
        int value = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return -1;
            }
            value = value * 10 + (c - '0');     // char arithmetic: '7' - '0' = 7
        }
        return value;
    }

    private static boolean readYesNo(String prompt) {
        String answer = readLine(prompt + " (y/n): ");
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    private static boolean isScanned() {
        if (FileStore.root == null) {
            System.out.println("Please scan a folder first (option 1).");
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // 1. Scan
    // ---------------------------------------------------------------
    private static void scanFolder() {
        String path = readLine("Folder path (Enter = " + DEMO_FOLDER + "): ");
        if (path.length() == 0) {
            path = DEMO_FOLDER;
        } else if (path.charAt(0) == '~') {
            // "~/Downloads" -> "/home/user/Downloads"
            path = System.getProperty("user.home").concat(path.substring(1));
        }

        File folder = new File(path).getAbsoluteFile();

        // Nested if-else: check one thing after another.
        if (!folder.exists()) {
            if (path.equals(DEMO_FOLDER)) {
                System.out.println("Demo folder not found. Create it with option 10.");
            } else {
                System.out.println("That folder does not exist.");
            }
            return;
        } else if (!folder.isDirectory()) {
            System.out.println("That is a file, not a folder.");
            return;
        }

        lastScanRecursive = readYesNo("Include sub-folders?");
        FileStore.root = folder;
        rescan();
        System.out.println("Scan complete: " + FileStore.count + " files found.");
    }

    private static void rescan() {
        FileStore.clear();
        FileStore.scan(FileStore.root, lastScanRecursive);
    }

    // ---------------------------------------------------------------
    // 2. Show files (array traversal)
    // ---------------------------------------------------------------
    private static void printTableHeader() {
        System.out.println(" No.  " + Display.fit("Name", 28) + Display.fit("Size", 11)
                + Display.fit("Category", 11) + "Flags");
        Display.line();
    }

    private static void printRow(int index) {
        File file = FileStore.files[index];
        System.out.printf(" %-4d %s%s%s%s%n",
                index + 1,
                Display.fit(file.getName(), 28),
                Display.fit(Display.formatSize(FileStore.sizes[index]), 11),
                Display.fit(FileStore.categories[index], 11),
                Categorizer.flagsToString(Categorizer.flagsOf(file)));
    }

    private static void showFiles() {
        Display.title("FILE LIST");
        if (FileStore.count == 0) {
            System.out.println("No files in the list.");
            return;
        }
        printTableHeader();
        for (int i = 0; i < FileStore.count; i++) {
            printRow(i);
        }
        Display.line();
        System.out.println("Flags: r = readable, w = writable, x = executable, B = 1 MB or bigger");
    }

    // ---------------------------------------------------------------
    // 3. Statistics (series, arithmetic operators, bar chart pattern)
    // ---------------------------------------------------------------
    private static void showStatistics() {
        Display.title("STATISTICS REPORT");
        int n = FileStore.count;
        if (n == 0) {
            System.out.println("No files in the list.");
            return;
        }

        // Series: total = size1 + size2 + ... + sizeN
        long totalBytes = 0;
        int[] perCategoryCount = new int[Categorizer.CATEGORIES.length];
        long[] perCategoryBytes = new long[Categorizer.CATEGORIES.length];

        for (int i = 0; i < n; i++) {
            totalBytes += FileStore.sizes[i];
            int c = Categorizer.indexOfCategory(FileStore.categories[i]);
            perCategoryCount[c]++;
            perCategoryBytes[c] += FileStore.sizes[i];
        }

        double average = (double) totalBytes / n;     // cast avoids integer division
        System.out.println("Total files  : " + n);
        System.out.println("Total size   : " + Display.formatSize(totalBytes, 2));
        System.out.println("Average size : " + Display.formatSize((long) average, 2));

        // Bar chart: the longest bar gets MAX_BAR characters.
        int highest = 0;
        for (int c = 0; c < perCategoryCount.length; c++) {
            if (perCategoryCount[c] > highest) {
                highest = perCategoryCount[c];
            }
        }
        System.out.println();
        System.out.println("Files per category:");
        for (int c = 0; c < perCategoryCount.length; c++) {
            if (perCategoryCount[c] == 0) {
                continue;
            }
            int length = perCategoryCount[c] * MAX_BAR / highest;
            int percent = perCategoryCount[c] * 100 / n;
            System.out.printf("  %-10s %-" + MAX_BAR + "s %3d  %3d%%  %s%n",
                    Categorizer.CATEGORIES[c], Display.bar(length, '#'),
                    perCategoryCount[c], percent,
                    Display.formatSize(perCategoryBytes[c]));
        }

        // Largest files, using the bubble-sorted index array.
        System.out.println();
        System.out.println("Largest files:");
        Display.triangle(3, 2);
        int[] order = FileStore.indexesBySizeDescending();
        for (int rank = 0; rank < order.length; rank++) {
            if (rank == TOP_FILES) {
                break;
            }
            int i = order[rank];
            System.out.println("  " + (rank + 1) + ". " + Display.fit(FileStore.files[i].getName(), 30)
                    + Display.formatSize(FileStore.sizes[i]));
        }

        // Series: sum of the natural numbers 1 + 2 + ... + (n-1).
        // To compare every file with every other file we need exactly
        // that many comparisons. Loop result and formula must agree.
        long bySeries = 0;
        for (int k = 1; k <= n - 1; k++) {
            bySeries += k;
        }
        long byFormula = (long) n * (n - 1) / 2;
        System.out.println();
        System.out.println("Pairs to check for duplicates (worst case):");
        System.out.println("  1 + 2 + ... + " + (n - 1) + " = " + bySeries
                + "   formula n(n-1)/2 = " + byFormula);
    }

    // ---------------------------------------------------------------
    // 4. Search
    // ---------------------------------------------------------------
    private static void searchFiles() {
        String keyword = readLine("Enter part of the file name: ");
        if (keyword.length() == 0) {
            System.out.println("Nothing to search for.");
            return;
        }
        int[] matches = new int[FileStore.count];
        int found = FileStore.search(keyword, matches);

        if (found == 0) {
            System.out.println("No file name contains \"" + keyword + "\".");
            return;
        }
        System.out.println(found + " match(es):");
        printTableHeader();
        for (int k = 0; k < found; k++) {
            printRow(matches[k]);
        }
    }

    // ---------------------------------------------------------------
    // 5. Exclude (array deletion). The file on disk is NOT deleted.
    // ---------------------------------------------------------------
    private static void excludeFile() {
        showFiles();
        if (FileStore.count == 0) {
            return;
        }
        int number = readInt("File number to exclude (0 = cancel): ");
        if (number == 0) {
            return;
        }
        if (number < 1 || number > FileStore.count) {
            System.out.println("There is no file with that number.");
            return;
        }
        String name = FileStore.files[number - 1].getName();
        FileStore.deleteAt(number - 1);
        System.out.println("\"" + name + "\" removed from the list. It is untouched on disk"
                + " and will be ignored until the next scan.");
    }

    // ---------------------------------------------------------------
    // 6. Duplicates
    // ---------------------------------------------------------------
    private static void findDuplicates() {
        Display.title("DUPLICATE FILES");
        int groups = DuplicateFinder.find();
        if (groups == 0) {
            System.out.println("No duplicate files found. (" + DuplicateFinder.comparisons
                    + " pairs checked)");
            return;
        }
        long wasted = DuplicateFinder.printGroups();
        System.out.println();
        Display.line();
        System.out.println(groups + " duplicate group(s), " + DuplicateFinder.comparisons
                + " pairs checked.");
        System.out.println("Space wasted by extra copies: " + Display.formatSize(wasted, 2));

        if (readYesNo("Move the [COPY] files into " + FileStore.DUPLICATE_FOLDER + "?")) {
            int moved = DuplicateFinder.moveExtraCopies();
            System.out.println(moved + " file(s) moved. Nothing was deleted; use option 8 to undo.");
            rescan();
        }
    }

    // ---------------------------------------------------------------
    // 7. Organize
    // ---------------------------------------------------------------
    private static void organizeFiles() {
        Display.title("ORGANIZE BY TYPE");
        System.out.println("Preview:");
        int planned = Organizer.preview();
        if (planned == 0) {
            System.out.println("  Everything is already organized.");
            return;
        }
        System.out.println();
        if (!readYesNo("Move these " + planned + " file(s)?")) {
            System.out.println("Cancelled. No files were moved.");
            return;
        }
        int moved = Organizer.organize();
        System.out.println(moved + " of " + planned + " file(s) moved. Use option 8 to undo.");
        rescan();
    }

    // ---------------------------------------------------------------
    // 8. Undo
    // ---------------------------------------------------------------
    private static void undoLastMove() {
        if (Organizer.getLogCount() == 0) {
            System.out.println("There is nothing to undo.");
            return;
        }
        System.out.println("Last moves:");
        System.out.print(Organizer.logSummary());
        if (!readYesNo("Put these files back?")) {
            return;
        }
        int restored = Organizer.undo();
        System.out.println(restored + " file(s) restored.");
        rescan();
    }

    // ---------------------------------------------------------------
    // 9. Tree (recursion vs iteration)
    // ---------------------------------------------------------------
    private static void showTree() {
        Display.title("FOLDER TREE");
        System.out.println("[" + FileStore.root.getName() + "]");
        FileStore.printTree(FileStore.root, 0, 4);

        long start = System.nanoTime();
        int recursive = FileStore.countFilesRecursive(FileStore.root);
        long middle = System.nanoTime();
        int iterative = FileStore.countFilesIterative(FileStore.root);
        long end = System.nanoTime();

        System.out.println();
        System.out.println("Counting every file in every sub-folder:");
        System.out.printf("  Recursive method : %d files in %d microseconds%n",
                recursive, (middle - start) / 1000);
        System.out.printf("  Iterative method : %d files in %d microseconds%n",
                iterative, (end - middle) / 1000);
    }

    // ---------------------------------------------------------------
    // 10. Demo folder
    // ---------------------------------------------------------------
    private static void createDemoFolder() {
        File folder = new File(DEMO_FOLDER).getAbsoluteFile();
        int written = DemoMaker.create(folder);
        if (written > 0) {
            System.out.println(written + " sample files created in " + folder.getPath());
            System.out.println("Now choose option 1 and press Enter to scan it.");
        }
    }
}
