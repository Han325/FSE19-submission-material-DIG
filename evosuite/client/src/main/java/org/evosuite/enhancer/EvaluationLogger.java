// IN FILE: EvaluationLogger.java

package org.evosuite.enhancer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A thread-safe, dedicated logger for evaluating Phase A performance.
 * Writes results to a structured CSV file for easy analysis.
 */
public class EvaluationLogger {

    private static PrintWriter writer;
    private static final Object lock = new Object(); // For thread safety

    static {
        try {
            // Create a unique filename for each run, e.g., "llm_eval_20230527-143055.csv"
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String filename = "llm_eval_run_" + timestamp + ".csv";
            
            // This will create the file in the root directory where the script is run
            writer = new PrintWriter(new FileWriter(filename, true));

            // Write the CSV header
            writer.println("Timestamp,CandidateTestCaseID,ParameterIndex,MethodName,ParameterJavaType,InitialValue,LLMSuggestion,LLMConfidence,LLMReasoning,Status,TimeToRespond_ms");
            writer.flush();
            
            // Ensure the file is closed when the JVM shuts down
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (lock) {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize EvaluationLogger", e);
        }
    }

    public static void log(
            String candidateId, int paramIndex, String methodName, String paramType,
            String initialValue, String suggestion, String confidence, String reasoning,
            String status, long duration) {
        
        synchronized (lock) {
            if (writer != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                
                // Escape commas and quotes to not break the CSV format
                String cleanReasoning = escapeCsv(reasoning);

                writer.printf("%s,%s,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d%n",
                        timestamp, candidateId, paramIndex, methodName, paramType,
                        initialValue, suggestion, confidence, cleanReasoning,
                        status, duration);
                writer.flush();
            }
        }
    }

    // Helper to make sure our CSV doesn't get broken by commas or quotes in the reasoning
    private static String escapeCsv(String data) {
        if (data == null) return "";
        String escapedData = data.replace("\"", "\"\"");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("\n")) {
            escapedData = "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}