// IN FILE: DebugStoryLogger.java
package org.evosuite.enhancer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A dedicated logger that writes a detailed, human-readable story of the
 * enhancement process for each candidate to a single file on the Desktop.
 */
public class DebugStoryLogger {

    private static PrintWriter writer;
    private static final Object lock = new Object(); // For thread safety

    static {
        try {
            // Get the user's home directory and create a path to the Desktop
            String desktopPath = System.getProperty("user.home") + "/Desktop/";
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            String filename = desktopPath + "llm_debug_story_" + timestamp + ".log";
            
            writer = new PrintWriter(new FileWriter(filename, true));
            System.out.println("DebugStoryLogger initialized. Debug story will be saved to: " + filename);
            
            // Add a shutdown hook to ensure the file is always closed properly
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                synchronized (lock) {
                    if (writer != null) {
                        writer.close();
                    }
                }
            }));

        } catch (IOException e) {
            System.err.println("FATAL: Could not initialize DebugStoryLogger: " + e.getMessage());
            e.printStackTrace();
            writer = null;
        }
    }

    // --- LOGGING METHODS ---

    public static void logCandidateStart(int candidateId, int batchSize) {
        log(String.format("==================== START ENHANCEMENT: CANDIDATE #%d (Batch Size: %d) ====================", candidateId, batchSize));
    }

    public static void logInitialTestCase(String code) {
        log("\n--- [1. INITIAL STATE] EvoSuite Generated Test Case ---\n" + code);
    }

    public static void logPrompt(String prompt) {
        log("\n--- [2. LLM PROMPT] Sending to Ollama ---\n" + prompt);
    }

    public static void logResponse(String response, long duration) {
        log(String.format("\n--- [3. LLM RESPONSE] Received in %.3f s ---", duration / 1000.0));
        log(response);
    }

    public static void logParamHeader(int index, int total, String paramName, String paramType, String initialValue) {
        log(String.format("\n--- [4. PROCESSING PARAM %d/%d] '%s' (%s) ---", index, total, paramName, paramType));
        log("Original EvoSuite Value: " + initialValue);
    }

    public static void logLLMSuggestion(String suggestionJson) {
        log("LLM Suggestion: " + suggestionJson);
    }
    
    public static void logDecision(String reason) {
        log("DECISION: " + reason);
    }

    public static void logApplicationAction(String action) {
        log("ACTION: " + action);
    }

    public static void logApplicationOutcome(boolean success, String message, Exception e) {
        if (success) {
            log("OUTCOME: SUCCESS. " + message);
        } else {
            log("OUTCOME: FAILED. " + message);
            if (e != null) {
                // Convert stack trace to a string to log it
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log("STACK TRACE:\n" + sw.toString());
            }
        }
    }
    
    public static void logFinalTestCase(String code) {
        log("\n--- [5. FINAL STATE] Modified Test Case ---\n" + code);
    }

    public static void logCandidateEnd(int candidateId, int applied, int skipped, int failed) {
        log(String.format("\n==================== FINISHED CANDIDATE #%d: Applied: %d | Skipped: %d | Failed: %d ====================\n\n",
                candidateId, applied, skipped, failed));
    }

     public static void logGIPhaseStart(int numSeeds, int variationsPerSeed, boolean isDryRun) {
        String dryRunMessage = isDryRun ? " [DRY RUN]" : "";
        log(String.format("==================== [PHASE B] STARTING GENETIC DIVERSIFICATION%s ====================", dryRunMessage));
        log(String.format("Processing %d seeds to generate %d variations each.", numSeeds, variationsPerSeed));
    }

    public static void logGISeedProcessingStart(int seedId, int seedNum, int totalSeeds) {
        log(String.format("\n--- Diversifying Seed #%d (Seed %d of %d) ---", seedId, seedNum, totalSeeds));
    }

    public static void logGIAuditionStart(int poolSize) {
        log("GI AUDITION: Generating " + poolSize + " potential mutations for this seed...");
    }

    public static void logGISelection(int variationNum, int totalVariations, String mutationType, double distance) {
        log(String.format("GI SELECTION (%d/%d): Chose candidate with mutation '%s'. Min-Distance to family: %.4f",
            variationNum, totalVariations, mutationType, distance));
    }

    public static void logGIPhaseEnd(int totalPopulationSize) {
        log(String.format("\n==================== [PHASE B] DIVERSIFICATION COMPLETE ===================="));
        log("Final supercharged population size: " + totalPopulationSize);
    }


    /**
     * The core private method that handles writing to the file safely.
     */
    public static void log(String message) {
        synchronized (lock) {
            if (writer != null) {
                writer.println(message);
                writer.flush();
            }
        }
    }
}