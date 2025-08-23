// IN FILE: src/main/java/org/evosuite/enhancer/LLMClient.java
package org.evosuite.enhancer;

/**
 * An interface for clients that communicate with a Large Language Model.
 * This allows for abstracting away the specific provider (e.g., Ollama, OpenAI).
 */
public interface LLMClient {

    /**
     * Sends a JSON-formatted prompt string to the LLM and returns the response.
     *
     * @param jsonPrompt The complete request payload, formatted as a JSON string.
     *                   The structure of this JSON is specific to the underlying provider.
     * @return The raw string response from the LLM, or null if an error occurred.
     */
    String generate(String jsonPrompt);
}