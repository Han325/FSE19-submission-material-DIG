// IN FILE: src/main/java/org/evosuite/enhancer/OpenAIClient.java

package org.evosuite.enhancer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIClient implements LLMClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIClient.class);
    
    // THIS IS THE URL YOU REQUESTED
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/responses";
    
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_MS = 120000;

    private final String apiKey;

    public OpenAIClient() {
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("FATAL: OPENAI_API_KEY environment variable not set.");
        }
        logger.info("OpenAIClient initialized for endpoint: {}", OPENAI_API_URL);
    }

    @Override
    public String generate(String genericJsonRequest) {
        String openAIRequestPayload;
        try {
            JSONObject genericRequest = new JSONObject(genericJsonRequest);
            String model = genericRequest.getString("model");
            String promptText = genericRequest.getString("prompt");

            JSONObject openAIRequest = new JSONObject();
            openAIRequest.put("model", model);
            
            // Using the 'messages' structure as it's the most modern format
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "You are an expert Test Data Analyst. Your output MUST be a valid JSON object."));
            messages.put(new JSONObject().put("role", "user").put("content", promptText));
            openAIRequest.put("messages", messages);

            openAIRequestPayload = openAIRequest.toString();

        } catch (Exception e) {
            logger.error("Failed to reformat prompt for OpenAI.", e);
            return null;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(OPENAI_API_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);

            logger.debug("Sending request to {} with model {}...", OPENAI_API_URL, new JSONObject(openAIRequestPayload).getString("model"));
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = openAIRequestPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            logger.debug("Received response code from OpenAI: {}", responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseBody = readStream(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                JSONObject responseJson = new JSONObject(responseBody);
                
                // Assuming the response structure might be { "choices": [ { "message": { "content": "..." } } ] }
                if (responseJson.has("choices")) {
                    String contentString = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                    JSONObject finalResponse = new JSONObject();
                    finalResponse.put("response", contentString);
                    return finalResponse.toString();
                } else {
                    // Fallback for an unknown structure
                    return responseBody;
                }

            } else {
                String errorResponse = readStream(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                logger.error("OpenAI request FAILED with status code: {} - {}", responseCode, errorResponse);
                return null;
            }

        } catch (IOException e) {
            logger.error("Failed to send request to OpenAI: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readStream(InputStreamReader streamReader) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(streamReader)) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine);
            }
        }
        return response.toString();
    }
}