// IN FILE: OllamaClient.java
// This version uses the older HttpURLConnection API, compatible with Java 8.

package org.evosuite.enhancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OllamaClient implements LLMClient {

    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    // --- IMPORTANT: CHANGE THIS TO YOUR DEVICE IP ADDRESS ---
    private static final String OLLAMA_HOST_IP = "10.0.2.2"; // Changed from "192.168.64.1"
    
    private static final String OLLAMA_API_URL = "http://" + OLLAMA_HOST_IP + ":11434/api/generate";
    // private static final String OLLAMA_API_URL = "https://b63057e8e5b0.ngrok-free.app:11434/api/generate";
    private static final int CONNECT_TIMEOUT_MS = 10000; // 10 seconds
    private static final int READ_TIMEOUT_MS = 120000;   // 2 minutes

    public OllamaClient() {
        logger.info("OllamaClient initialized for host: {}", OLLAMA_API_URL);
    }

    @Override
    public String generate(String jsonPrompt) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(OLLAMA_API_URL);
            conn = (HttpURLConnection) url.openConnection();

            // Set up the connection for a POST request
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true); // This is required for POST
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            // Write the JSON prompt to the request body
            logger.debug("Sending request to Ollama...");
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPrompt.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = conn.getResponseCode();
            logger.debug("Received response code: {}", responseCode);
            
            // Read the response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readStream(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                String errorResponse = readStream(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                logger.error("Ollama request failed with status code: {} - {}", responseCode, errorResponse);
                return null;
            }

        } catch (IOException e) {
            logger.error("Failed to send request to Ollama: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Helper method to read the response stream into a string
    private String readStream(InputStreamReader streamReader) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(streamReader)) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        return response.toString();
    }
}