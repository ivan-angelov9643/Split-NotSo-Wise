package bg.sofia.uni.fmi.mjt.splitnotsowise.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ErrorLogger {
    private static final String PROJECT_NAME = "Client";
    private static final String ERROR_LOGS_FILE_NAME = "error_logs.txt";
    private static final Path ERROR_LOGS_FILE_PATH = Paths.get(PROJECT_NAME, ERROR_LOGS_FILE_NAME);
    public static void log(Exception e) {
        try (PrintWriter writer =
                 new PrintWriter(new BufferedWriter(new FileWriter(ERROR_LOGS_FILE_PATH.toString(), true)))) {
            writer.println("Exception occurred at: " + System.currentTimeMillis());
            writer.println("Exception type: " + e.getClass().getName());
            writer.println("Exception message: " + e.getMessage());
            writer.println("Stack trace:");
            e.printStackTrace(writer);
            writer.println();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
