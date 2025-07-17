package code.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogExporter {

    /**
     * This method is called AFTER EACH TEST. It retrieves logs from the current
     * driver and adds them to a master list that persists for the whole suite.
     */
    public static void collectLogs(WebDriver driver, List<LogEntry> masterLogList) {
        try {
            // Quietly add logs. No need for console output here.
            masterLogList.addAll(driver.manage().logs().get(LogType.BROWSER).getAll());
        } catch (Exception e) {
            // Log if collection fails for a specific test, but don't stop the suite.
            System.err.println("[Log Collector]: Warning - could not collect logs for a test. " + e.getMessage());
        }
    }

    /**
     * This method is called ONCE at the end of the entire suite.
     * It takes the master list of all collected logs and writes them to the files.
     */
    public static void exportCollectedLogsToFile(List<LogEntry> masterLogList) {
        try {
            System.out.println("\n[Log Export]: Processing all collected logs and writing to files...");

            String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
            File desktopDir = new File(desktopPath);
            if (!desktopDir.exists()) {
                desktopDir.mkdirs();
            }

            File rawLogFile = new File(desktopPath, "raw_browser_logs.txt");
            File filteredLogFile = new File(desktopPath, "unique_faults.txt");

            try (PrintWriter rawWriter = new PrintWriter(new FileWriter(rawLogFile));
                 PrintWriter filteredWriter = new PrintWriter(new FileWriter(filteredLogFile))) {

                Set<String> uniqueErrors = new HashSet<>();

                rawWriter.println("--- Raw Browser Console Logs (from all tests) ---");
                for (LogEntry entry : masterLogList) {
                    String logLine = new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage();
                    rawWriter.println(logLine);
                    if ("SEVERE".equals(entry.getLevel().getName())) {
                        uniqueErrors.add(entry.getMessage());
                    }
                }
                rawWriter.println("--- End of Raw Logs ---");

                filteredWriter.println("--- Unique JavaScript Faults (from all tests) ---");
                for (String error : uniqueErrors) {
                    filteredWriter.println(error);
                }
                filteredWriter.println("--- End of Unique Faults ---");

                System.out.println("[Log Export]: Success. Raw logs at: " + rawLogFile.getAbsolutePath());
                System.out.println("[Log Export]: Found " + uniqueErrors.size() + " unique faults. Filtered logs at: " + filteredLogFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.out.println("[Log Export]: FAILED to write final log files. Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}