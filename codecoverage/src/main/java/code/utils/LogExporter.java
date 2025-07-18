package code.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LogEntries;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogExporter {

    private static final Set<String> cumulativeUniqueErrors = new HashSet<>();
    private static int testCaseCounter = 0;

    public static void processLogsAfterTest(WebDriver driver, List<LogEntry> masterLogList) {
        testCaseCounter++;
        System.out.println("\n--- Processing logs for Test Case #" + testCaseCounter + " ---");

        try {
            // STEP 1: Get logs from the driver ONCE and only ONCE.
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            List<LogEntry> currentLogs = logEntries.getAll();
            if (currentLogs.isEmpty()) {
                System.out.println("  [DEBUG] No new logs found in driver for this test case.");
            }

            // STEP 2: DO THE JOB OF THE OLD 'collectLogs' METHOD
            // Add these new logs to the master list for the final .txt export.
            masterLogList.addAll(currentLogs);

            // STEP 3: DO THE JOB OF THE OLD 'recordCumulativeFaults' METHOD
            // Iterate through the SAME logs we just got to update the cumulative count.
            int newErrorsFoundThisRun = 0;
            for (LogEntry entry : currentLogs) { // We use the logs we just fetched!
                if ("SEVERE".equals(entry.getLevel().getName())) {
                    if (cumulativeUniqueErrors.add(entry.getMessage())) {
                        newErrorsFoundThisRun++;
                        System.out.println("  [DEBUG] NEW UNIQUE SEVERE FAULT FOUND!");
                    }
                }
            }
            if (newErrorsFoundThisRun > 0) {
                System.out.println(
                        "  [DEBUG] Found " + newErrorsFoundThisRun + " new unique SEVERE errors in this test.");
            }

        } catch (Exception e) {
            System.err.println(
                    "[Log Processor]: FAILED to process logs for test #" + testCaseCounter + ". " + e.getMessage());
        }

        // STEP 4: Write the LATEST cumulative count to the CSV file.
        int currentFaultCount = cumulativeUniqueErrors.size();
        System.out
                .println("  >>> After test #" + testCaseCounter + ", cumulative unique faults = " + currentFaultCount);

        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        File resultsFile = new File(desktopPath, "fault_discovery_rate.csv");

        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsFile, true))) {
            if (resultsFile.length() == 0) {
                writer.println("test_case_number,cumulative_unique_faults");
            }
            writer.println(testCaseCounter + "," + currentFaultCount);
        } catch (Exception e) {
            System.err.println("[Discovery Rate]: FAILED to write to CSV on Desktop. " + e.getMessage());
        }
    }

    public static void initialize() {
        testCaseCounter = 0;
        cumulativeUniqueErrors.clear();
        String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";
        File resultsFile = new File(desktopPath, "fault_discovery_rate.csv");
        if (resultsFile.exists()) {
            if (resultsFile.delete()) {
                System.out.println("[Discovery Rate]: Cleared previous fault_discovery_rate.csv from Desktop.");
            }
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