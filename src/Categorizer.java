import java.io.File;

/*
 * Categorizer.java
 * Decides which category a file belongs to by looking at its name.
 *
 * Syllabus topics shown in this file:
 *   - String methods: lastIndexOf, substring, toLowerCase, length,
 *     charAt, equals
 *   - switch statement with many cases (a real use case for switch)
 *   - Bitwise operators: | to combine flags, & to test them
 */
public class Categorizer {

    public static final String[] CATEGORIES = {
        "Images", "Documents", "Videos", "Music", "Archives", "Code", "Others"
    };

    // Each attribute is one bit, so several fit in a single int.
    //   READ    = 0001
    //   WRITE   = 0010
    //   EXECUTE = 0100
    //   BIG     = 1000
    public static final int FLAG_READ = 1;
    public static final int FLAG_WRITE = 1 << 1;
    public static final int FLAG_EXECUTE = 1 << 2;
    public static final int FLAG_BIG = 1 << 3;
    private static final long BIG_FILE_BYTES = 1024 * 1024;   // 1 MB

    /* "Photo.JPG" -> "jpg".  Returns "" when there is no extension. */
    public static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    /* Several case labels share one block: that is "fall through". */
    public static String categoryOf(String fileName) {
        String ext = extensionOf(fileName);
        switch (ext) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
            case "svg":
                return "Images";

            case "txt":
            case "pdf":
            case "doc":
            case "docx":
            case "xls":
            case "xlsx":
            case "ppt":
            case "pptx":
            case "csv":
            case "md":
            case "odt":
                return "Documents";

            case "mp4":
            case "mkv":
            case "avi":
            case "mov":
            case "webm":
                return "Videos";

            case "mp3":
            case "wav":
            case "flac":
            case "ogg":
            case "m4a":
                return "Music";

            case "zip":
            case "rar":
            case "tar":
            case "gz":
            case "7z":
                return "Archives";

            case "java":
            case "py":
            case "c":
            case "cpp":
            case "js":
            case "html":
            case "css":
            case "sh":
                return "Code";

            default:
                return "Others";
        }
    }

    /* Position of a category inside CATEGORIES, or -1 (linear search). */
    public static int indexOfCategory(String category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equals(category)) {
                return i;
            }
        }
        return -1;
    }

    /* Packs four yes/no facts about a file into one int using bitwise OR. */
    public static int flagsOf(File file) {
        int flags = 0;
        if (file.canRead()) {
            flags = flags | FLAG_READ;
        }
        if (file.canWrite()) {
            flags |= FLAG_WRITE;
        }
        if (file.canExecute()) {
            flags |= FLAG_EXECUTE;
        }
        if (file.length() >= BIG_FILE_BYTES) {
            flags |= FLAG_BIG;
        }
        return flags;
    }

    /* Unpacks the flags with bitwise AND, e.g. 0011 -> "rw--". */
    public static String flagsToString(int flags) {
        StringBuilder sb = new StringBuilder();
        sb.append((flags & FLAG_READ) != 0 ? 'r' : '-');
        sb.append((flags & FLAG_WRITE) != 0 ? 'w' : '-');
        sb.append((flags & FLAG_EXECUTE) != 0 ? 'x' : '-');
        sb.append((flags & FLAG_BIG) != 0 ? 'B' : '-');
        return sb.toString();
    }
}
