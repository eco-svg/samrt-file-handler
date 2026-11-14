import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * DemoMaker.java
 * Creates a small messy folder full of sample files (some of them
 * identical) so the program can be demonstrated safely without
 * touching any real files.
 */
public class DemoMaker {

    // Parallel arrays: NAMES[i] is written with the text CONTENTS[i].
    private static final String[] NAMES = {
        "report.txt", "report_copy.txt", "notes.txt", "marks.csv",
        "photo.jpg", "photo (1).jpg", "holiday.png",
        "song.mp3", "movie.mp4", "backup.zip",
        "Hello.java", "script.py", "README",
        "old/report_backup.txt", "old/song.mp3", "old/todo.txt"
    };

    private static final String REPORT = "Quarterly report\nSales went up by 12 percent.\n";
    private static final String PHOTO = "FAKE-JPG-DATA-0123456789-0123456789-0123456789";
    private static final String SONG = "FAKE-MP3-DATA la la la la la la la la la la la";

    private static final String[] CONTENTS = {
        REPORT, REPORT, "Buy milk. Finish Java project.\n", "name,marks\nAsha,91\nRavi,84\n",
        PHOTO, PHOTO, "FAKE-PNG-DATA beach and sunset",
        SONG, "FAKE-MP4-DATA frame frame frame frame", "FAKE-ZIP-DATA compressed bytes",
        "class Hello { }\n", "print('hello')\n", "This file has no extension.\n",
        REPORT, SONG, "1. Submit project\n2. Sleep\n"
    };

    /* Returns the number of files written, or -1 on failure. */
    public static int create(File folder) {
        int written = 0;
        for (int i = 0; i < NAMES.length; i++) {
            File file = new File(folder, NAMES[i]);
            file.getParentFile().mkdirs();
            try (FileWriter out = new FileWriter(file)) {
                out.write(CONTENTS[i]);
                written++;
            } catch (IOException e) {
                System.out.println("Could not write " + file.getPath());
                return -1;
            }
        }
        return written;
    }
}
