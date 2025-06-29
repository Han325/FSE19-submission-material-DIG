// IN FILE: PromptLogger.java
package org.evosuite.enhancer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple, dedicated logger to save the exact prompts being sent to the LLM.
 * Writes each full request payload to a file for external benchmarking and debugging.
 */
public class PromptLogger {

    private static PrintWriter writer;
    private static final Object lock = new Object(); // For thread safety

    static {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String filename = "llm_prompts_" + timestamp + ".log";
            
            writer = new PrintWriter(new FileWriter(filename, true));
            
            // Add a shutdown hook to ensure the file is always closed properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (lock) {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }));

        } catch (IOException e) {
            // If we can't create the log file, we shouldn't crash the whole program.
            // Log to standard error instead.
            System.err.println("FATAL: Could not initialize PromptLogger: " + e.getMessage());
            e.printStackTrace();
            writer = null; // Ensure writer is null so we don't try to use it
        }
    }

    /**
     * Logs the full JSON payload of a request to the file.
     * @param requestPayload The exact string being sent to the Ollama client.
     */
    public static void log(String requestPayload) {
        synchronized (lock) {
            if (writer != null) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                writer.println("--- PROMPT @ " + timestamp + " ---");
                writer.println(requestPayload);
                writer.println("--- END OF PROMPT ---");
                writer.println(); // Add a blank line for readability
                writer.flush();
            }
        }
    }
}