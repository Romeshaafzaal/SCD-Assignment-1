import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Logger {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = LOG_DIR + "/campus_log.txt";
    private static boolean enabled = true;

    public static void setEnabled(boolean value) {
        enabled = value;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void log(String message) {
        if (!enabled) {
            return;
        }
        String entry = "[" + LocalDateTime.now() + "] " + message;
        System.out.println(entry);
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (SecurityException e) {
            System.err.println("Could not create log folder: " + e.getMessage());
        }
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(entry + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }

    public static void logError(String context, Exception e) {
        log("ERROR [" + context + "]: " + e.getMessage());
    }
}