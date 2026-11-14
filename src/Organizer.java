import java.io.File;

/*
 * Organizer.java
 * Moves files into category folders (Images, Documents, ...) and
 * remembers every move so it can be undone.
 *
 * Syllabus topics shown in this file:
 *   - Methods with parameters and return types, access modifiers
 *   - Call by value
 *   - StringBuilder vs StringBuffer
 *   - while loop, for loop counting backwards
 */
public class Organizer {

    // Move log: logFrom[i] was moved to logTo[i].
    // private = only this class can touch these arrays directly.
    private static File[] logFrom = new File[0];
    private static File[] logTo = new File[0];
    private static int logCount = 0;

    public static void resetLog(int capacity) {
        logFrom = new File[capacity];
        logTo = new File[capacity];
        logCount = 0;
    }

    public static int getLogCount() {
        return logCount;
    }

    /*
     * Builds a name that is not taken yet inside targetFolder:
     *   song.mp3 -> song_1.mp3 -> song_2.mp3 ...
     *
     * StringBuilder is used because the name is assembled from pieces.
     * A String is immutable, so "a" + "b" + "c" in a loop creates a new
     * object every time; StringBuilder edits one buffer instead.
     */
    private static File freeTarget(File targetFolder, String fileName) {
        File target = new File(targetFolder, fileName);
        if (!target.exists()) {
            return target;
        }
        // CALL BY VALUE: "fileName" is a copy of the caller's value.
        // Assigning to "base", "ext" or even "fileName" in here can
        // never change the variable the caller passed in.
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);      // includes the dot
        }
        int number = 1;
        while (target.exists()) {
            StringBuilder name = new StringBuilder(base);
            name.append('_').append(number).append(ext);
            target = new File(targetFolder, name.toString());
            number++;
        }
        return target;
    }

    /* Moves one file into targetFolder and records it. true = success. */
    public static boolean moveFile(File source, File targetFolder) {
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            return false;
        }
        File target = freeTarget(targetFolder, source.getName());
        if (!source.renameTo(target)) {
            return false;
        }
        logFrom[logCount] = source;
        logTo[logCount] = target;
        logCount++;
        return true;
    }

    /* A file needs moving unless it already sits in its category folder. */
    private static boolean needsMove(int index) {
        File target = new File(FileStore.root, FileStore.categories[index]);
        return !target.equals(FileStore.files[index].getParentFile());
    }

    /* Shows what WOULD happen without touching anything. Returns the count. */
    public static int preview() {
        int planned = 0;
        for (int i = 0; i < FileStore.count; i++) {
            if (!needsMove(i)) {
                continue;
            }
            System.out.println("  " + Display.fit(FileStore.relativePath(FileStore.files[i]), 36)
                    + " ->  " + FileStore.categories[i] + File.separator);
            planned++;
        }
        return planned;
    }

    /* Performs the moves. Returns how many files were moved. */
    public static int organize() {
        resetLog(FileStore.count);
        int moved = 0;
        for (int i = 0; i < FileStore.count; i++) {
            if (!needsMove(i)) {
                continue;
            }
            File target = new File(FileStore.root, FileStore.categories[i]);
            if (moveFile(FileStore.files[i], target)) {
                moved++;
            } else {
                System.out.println("  Could not move: " + FileStore.files[i].getName());
            }
        }
        return moved;
    }

    /*
     * Reverses the last organize / duplicate move. The log is walked
     * BACKWARDS so the most recent move is undone first.
     */
    public static int undo() {
        int restored = 0;
        for (int i = logCount - 1; i >= 0; i--) {
            File original = logFrom[i];
            File current = logTo[i];
            if (!current.exists() || original.exists()) {
                continue;                       // changed by someone else: leave it
            }
            original.getParentFile().mkdirs();
            if (current.renameTo(original)) {
                restored++;
                current.getParentFile().delete();   // only succeeds if now empty
            }
        }
        logCount = 0;
        return restored;
    }

    /*
     * Builds a text summary of the log.
     *
     * StringBuffer does the same job as StringBuilder, but its methods
     * are synchronized (safe when several threads share it), which
     * makes it a little slower. This program has one thread, so
     * StringBuilder is the normal choice; StringBuffer is used here
     * only to show that the two are used the same way.
     */
    public static String logSummary() {
        StringBuffer text = new StringBuffer();
        for (int i = 0; i < logCount; i++) {
            text.append("  ").append(i + 1).append(". ");
            text.append(logFrom[i].getName());
            text.append("  ->  ");
            text.append(FileStore.relativePath(logTo[i]));
            text.append('\n');
        }
        return text.toString();
    }

}
