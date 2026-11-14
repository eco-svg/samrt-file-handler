import java.io.File;

/*
 * FileStore.java
 * Holds the list of scanned files in plain single-dimensional arrays
 * (no ArrayList) and implements the common array operations by hand.
 *
 * Three "parallel" arrays are used: index i in each array describes
 * the same file.
 *
 *      files[i]       sizes[i]      categories[i]
 *      report.txt     1200          "Documents"
 *
 * Syllabus topics shown in this file:
 *   - Array declaration, initialization, memory allocation with new
 *   - Insertion, deletion, traversal, searching, sorting
 *   - Recursion (scanning folders inside folders)
 *   - Recursion vs iteration (two ways to count files)
 *   - break / continue
 */
public class FileStore {

    private static final int INITIAL_CAPACITY = 16;
    public static final String DUPLICATE_FOLDER = "_Duplicates";

    // Declaration + memory allocation. "new" reserves space on the heap.
    public static File[] files = new File[INITIAL_CAPACITY];
    public static long[] sizes = new long[INITIAL_CAPACITY];
    public static String[] categories = new String[INITIAL_CAPACITY];

    public static int count = 0;     // how many slots are actually used
    public static File root = null;  // folder that was scanned

    public static void clear() {
        files = new File[INITIAL_CAPACITY];
        sizes = new long[INITIAL_CAPACITY];
        categories = new String[INITIAL_CAPACITY];
        count = 0;
    }

    // ---------------------------------------------------------------
    // INSERTION (at the end). An array has a fixed length, so when it
    // is full we allocate a bigger one and copy everything across.
    // ---------------------------------------------------------------
    public static void insert(File file) {
        if (count == files.length) {
            grow();
        }
        files[count] = file;
        sizes[count] = file.length();
        categories[count] = Categorizer.categoryOf(file.getName());
        count++;
    }

    private static void grow() {
        int newCapacity = files.length << 1;   // bitwise left shift = x 2

        File[] newFiles = new File[newCapacity];
        long[] newSizes = new long[newCapacity];
        String[] newCategories = new String[newCapacity];

        for (int i = 0; i < count; i++) {
            newFiles[i] = files[i];
            newSizes[i] = sizes[i];
            newCategories[i] = categories[i];
        }
        files = newFiles;
        sizes = newSizes;
        categories = newCategories;
    }

    // ---------------------------------------------------------------
    // DELETION. Every element after the removed one moves one step
    // left to close the gap.
    // ---------------------------------------------------------------
    public static boolean deleteAt(int index) {
        if (index < 0 || index >= count) {
            return false;
        }
        for (int i = index; i < count - 1; i++) {
            files[i] = files[i + 1];
            sizes[i] = sizes[i + 1];
            categories[i] = categories[i + 1];
        }
        count--;
        files[count] = null;   // clear the now-unused last slot
        return true;
    }

    // ---------------------------------------------------------------
    // SEARCHING (linear search). Stores the matching indexes in
    // "result" and returns how many were found.
    // ---------------------------------------------------------------
    public static int search(String keyword, int[] result) {
        String key = keyword.toLowerCase();
        int found = 0;
        for (int i = 0; i < count; i++) {
            String name = files[i].getName().toLowerCase();
            if (name.indexOf(key) == -1) {
                continue;              // not a match, try the next file
            }
            result[found] = i;
            found++;
        }
        return found;
    }

    // ---------------------------------------------------------------
    // SORTING (bubble sort). Returns the file indexes ordered from the
    // largest file to the smallest. The real arrays are not changed.
    // ---------------------------------------------------------------
    public static int[] indexesBySizeDescending() {
        int[] order = new int[count];
        for (int i = 0; i < count; i++) {
            order[i] = i;
        }
        for (int pass = 0; pass < count - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < count - 1 - pass; j++) {
                if (sizes[order[j]] < sizes[order[j + 1]]) {
                    int temp = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;                 // already sorted, stop early
            }
        }
        return order;
    }

    // ---------------------------------------------------------------
    // RECURSION: scanning a folder.
    // A folder can contain folders, which can contain folders... The
    // method calls itself for every sub-folder. The base case is a
    // folder with no sub-folders (or recursive == false).
    // ---------------------------------------------------------------
    public static void scan(File folder, boolean recursive) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return;                    // not readable
        }
        for (int i = 0; i < entries.length; i++) {
            File entry = entries[i];
            if (shouldSkip(entry)) {
                continue;
            }
            if (entry.isFile()) {
                insert(entry);
            } else if (entry.isDirectory() && recursive) {
                scan(entry, true);     // recursive call
            }
        }
    }

    /* Hidden files, symbolic links and our own _Duplicates folder are ignored. */
    public static boolean shouldSkip(File entry) {
        String name = entry.getName();
        if (name.charAt(0) == '.') {
            return true;
        }
        if (java.nio.file.Files.isSymbolicLink(entry.toPath())) {
            return true;
        }
        return entry.isDirectory() && name.equals(DUPLICATE_FOLDER);
    }

    // ---------------------------------------------------------------
    // RECURSION vs ITERATION: the same job done both ways.
    // ---------------------------------------------------------------

    /* Recursive: short and easy to read, but each call uses stack memory. */
    public static int countFilesRecursive(File folder) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < entries.length; i++) {
            if (shouldSkip(entries[i])) {
                continue;
            }
            if (entries[i].isDirectory()) {
                total = total + countFilesRecursive(entries[i]);
            } else {
                total++;
            }
        }
        return total;
    }

    /* Iterative: we keep our own "to do" stack of folders in an array. */
    public static int countFilesIterative(File folder) {
        File[] stack = new File[64];
        int top = 0;
        stack[top++] = folder;
        int total = 0;

        while (top > 0) {
            File current = stack[--top];
            File[] entries = current.listFiles();
            if (entries == null) {
                continue;
            }
            for (int i = 0; i < entries.length; i++) {
                if (shouldSkip(entries[i])) {
                    continue;
                }
                if (entries[i].isDirectory()) {
                    if (top == stack.length) {         // stack full: grow it
                        File[] bigger = new File[stack.length * 2];
                        for (int k = 0; k < top; k++) {
                            bigger[k] = stack[k];
                        }
                        stack = bigger;
                    }
                    stack[top++] = entries[i];
                } else {
                    total++;
                }
            }
        }
        return total;
    }

    /* Recursively prints the folder as an indented tree. */
    public static void printTree(File folder, int depth, int maxDepth) {
        File[] entries = folder.listFiles();
        if (entries == null) {
            return;
        }
        java.util.Arrays.sort(entries);
        for (int i = 0; i < entries.length; i++) {
            if (shouldSkip(entries[i])) {
                continue;
            }
            for (int d = 0; d < depth; d++) {
                System.out.print("|   ");
            }
            if (entries[i].isDirectory()) {
                System.out.println("+-- [" + entries[i].getName() + "]");
                if (depth + 1 < maxDepth) {
                    printTree(entries[i], depth + 1, maxDepth);
                }
            } else {
                System.out.println("+-- " + entries[i].getName());
            }
        }
    }

    /* Path of a file relative to the scanned folder, for shorter output. */
    public static String relativePath(File file) {
        String full = file.getAbsolutePath();
        String base = root.getAbsolutePath();
        if (full.startsWith(base) && full.length() > base.length()) {
            return full.substring(base.length() + 1);
        }
        return full;
    }
}
