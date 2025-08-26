package org.evosuite.enhancer;

import org.json.JSONObject;

/**
 * Mock implementation of LLMClient for testing purposes.
 * This allows us to test the enhancer functionality without making actual LLM calls.
 */
public class MockLLMClient implements LLMClient {
    
    private String predefinedResponse;
    private boolean shouldFail = false;
    private long simulatedDelay = 0;
    private int callCount = 0;
    
    public MockLLMClient() {
        // Default response that should work for most test cases
        JSONObject response = new JSONObject();
        response.put("reasoning_for_change_or_keep", "Test reasoning");
        response.put("suggested_value_as_string", "test_suggestion");
        response.put("confidence_low_medium_high", "high");
        
        JSONObject fullResponse = new JSONObject();
        fullResponse.put("response", response.toString());
        fullResponse.put("model", "test-model");
        
        this.predefinedResponse = fullResponse.toString();
    }
    
    /**
     * Sets a predefined response that will be returned by generate()
     */
    public void setPredefinedResponse(String response) {
        this.predefinedResponse = response;
    }
    
    /**
     * Sets a predefined response using a structured approach
     */
    public void setPredefinedResponse(String reasoning, String suggestedValue, String confidence) {
        JSONObject response = new JSONObject();
        response.put("reasoning_for_change_or_keep", reasoning);
        response.put("suggested_value_as_string", suggestedValue);
        response.put("confidence_low_medium_high", confidence);
        
        JSONObject fullResponse = new JSONObject();
        fullResponse.put("response", response.toString());
        fullResponse.put("model", "test-model");
        
        this.predefinedResponse = fullResponse.toString();
    }
    
    /**
     * Configure the mock to simulate failures
     */
    public void setShouldFail(boolean shouldFail) {
        this.shouldFail = shouldFail;
    }
    
    /**
     * Set a simulated delay in milliseconds
     */
    public void setSimulatedDelay(long delay) {
        this.simulatedDelay = delay;
    }
    
    /**
     * Get the number of times generate() has been called
     */
    public int getCallCount() {
        return callCount;
    }
    
    /**
     * Reset the call count
     */
    public void resetCallCount() {
        this.callCount = 0;
    }

    @Override
    public String generate(String jsonPrompt) {
        callCount++;
        
        if (simulatedDelay > 0) {
            try {
                Thread.sleep(simulatedDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        if (shouldFail) {
            return null; // Simulate failure
        }
        
        return predefinedResponse;
    }
    
    /**
     * Get the last prompt that was sent to generate()
     */
    private String lastPrompt;
    
    public String getLastPrompt() {
        return lastPrompt;
    }
    
    // Override generate to also capture the prompt
    public String generateWithPromptCapture(String jsonPrompt) {
        this.lastPrompt = jsonPrompt;
        return generate(jsonPrompt);
    }
}