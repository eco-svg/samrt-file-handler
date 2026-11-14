import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/*
 * DuplicateFinder.java
 * Finds files whose CONTENT is identical, even if the names differ.
 *
 * How it works (cheapest check first):
 *   1. Different size            -> cannot be duplicates, skip.
 *   2. Same size                 -> compute a hash (fingerprint) of each.
 *   3. Same size and same hash   -> compare byte by byte to be certain.
 *
 * Syllabus topics shown in this file:
 *   - Bitwise operators: XOR (^), AND (&), unsigned shift (>>>)
 *   - Nested loops with break and continue
 *   - Arrays used as lookup tables (groupOf, hashes)
 *   - Series: 1 + 2 + ... + (n-1) comparisons in the worst case
 */
public class DuplicateFinder {

    // FNV-1a hash constants (a well known simple hash function).
    private static final long FNV_OFFSET = 0xcbf29ce484222325L;
    private static final long FNV_PRIME = 0x100000001b3L;
    private static final String HEX_DIGITS = "0123456789abcdef";

    // groupOf[i] = duplicate group number of file i, or -1 if unique.
    public static int[] groupOf = new int[0];
    public static int groupCount = 0;
    public static long comparisons = 0;

    /*
     * Reads the file and mixes every byte into a 64-bit number.
     *   hash = hash XOR byte
     *   hash = hash * prime
     * Two files with different content almost always get different hashes.
     */
    public static long hashOf(File file) {
        long hash = FNV_OFFSET;
        byte[] buffer = new byte[8192];
        try (FileInputStream in = new FileInputStream(file)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    hash = hash ^ (buffer[i] & 0xff);   // & 0xff makes the byte 0..255
                    hash = hash * FNV_PRIME;
                }
            }
        } catch (IOException e) {
            return 0;      // unreadable file; sameContent() will reject it too
        }
        return hash;
    }

    /*
     * Converts a long to 16 hex characters by hand.
     * Each hex digit is 4 bits: shift the wanted 4 bits to the right
     * end, then mask everything else away with & 0xF.
     */
    public static String toHex(long value) {
        StringBuilder sb = new StringBuilder();
        for (int shift = 60; shift >= 0; shift -= 4) {
            int digit = (int) ((value >>> shift) & 0xF);
            sb.append(HEX_DIGITS.charAt(digit));
        }
        return sb.toString();
    }

    /* Final proof: compares the two files one byte at a time. */
    public static boolean sameContent(File a, File b) {
        try (BufferedInputStream inA = new BufferedInputStream(new FileInputStream(a));
             BufferedInputStream inB = new BufferedInputStream(new FileInputStream(b))) {
            while (true) {                 // infinite loop, left with return
                int byteA = inA.read();
                int byteB = inB.read();
                if (byteA != byteB) {
                    return false;
                }
                if (byteA == -1) {
                    return true;           // both ended together
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    /* Fills groupOf[] and returns the number of duplicate groups. */
    public static int find() {
        int n = FileStore.count;
        groupOf = new int[n];
        long[] hashes = new long[n];
        boolean[] hashed = new boolean[n];   // boolean arrays start as false
        for (int i = 0; i < n; i++) {
            groupOf[i] = -1;
        }
        groupCount = 0;
        comparisons = 0;

        for (int i = 0; i < n; i++) {
            if (groupOf[i] != -1) {
                continue;                  // already placed in a group
            }
            if (FileStore.sizes[i] == 0) {
                continue;                  // empty files are not interesting
            }
            boolean foundCopy = false;

            for (int j = i + 1; j < n; j++) {
                if (groupOf[j] != -1) {
                    continue;
                }
                comparisons++;
                if (FileStore.sizes[i] != FileStore.sizes[j]) {
                    continue;              // step 1: sizes differ
                }
                // Step 2: hash only when needed, and only once per file.
                if (!hashed[i]) {
                    hashes[i] = hashOf(FileStore.files[i]);
                    hashed[i] = true;
                }
                if (!hashed[j]) {
                    hashes[j] = hashOf(FileStore.files[j]);
                    hashed[j] = true;
                }
                // Step 3: && is short-circuit, so the slow byte compare
                // runs only when the hashes already match.
                if (hashes[i] == hashes[j]
                        && sameContent(FileStore.files[i], FileStore.files[j])) {
                    groupOf[j] = groupCount;
                    foundCopy = true;
                }
            }

            if (foundCopy) {
                groupOf[i] = groupCount;
                groupCount++;
            }
        }
        return groupCount;
    }

    /* Prints every group and returns the bytes wasted by extra copies. */
    public static long printGroups() {
        long wasted = 0;
        for (int g = 0; g < groupCount; g++) {
            boolean first = true;
            for (int i = 0; i < FileStore.count; i++) {
                if (groupOf[i] != g) {
                    continue;
                }
                if (first) {
                    System.out.println();
                    System.out.println("Group " + (g + 1) + "  |  "
                            + Display.formatSize(FileStore.sizes[i]) + " each  |  hash "
                            + toHex(hashOf(FileStore.files[i])));
                    System.out.println("   [KEEP] " + FileStore.relativePath(FileStore.files[i]));
                    first = false;
                } else {
                    System.out.println("   [COPY] " + FileStore.relativePath(FileStore.files[i]));
                    wasted += FileStore.sizes[i];
                }
            }
        }
        return wasted;
    }

    /*
     * Moves every extra copy into the _Duplicates folder. Nothing is
     * deleted, and the move can be reversed with "Undo".
     * Returns the number of files moved.
     */
    public static int moveExtraCopies() {
        File target = new File(FileStore.root, FileStore.DUPLICATE_FOLDER);
        boolean[] keptOne = new boolean[groupCount];
        int moved = 0;

        Organizer.resetLog(FileStore.count);
        for (int i = 0; i < FileStore.count; i++) {
            int g = groupOf[i];
            if (g == -1) {
                continue;
            }
            if (!keptOne[g]) {
                keptOne[g] = true;         // first file of the group stays
                continue;
            }
            if (Organizer.moveFile(FileStore.files[i], target)) {
                moved++;
            }
        }
        return moved;
    }
}
