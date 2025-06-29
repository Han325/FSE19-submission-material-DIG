package org.evosuite.enhancer;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.evosuite.testcase.statements.numeric.*; // For IntPrimitiveStatement, DoublePrimitiveStatement etc.
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.EnumPrimitiveStatement;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LLMInputEnhancer {

    private static final Logger logger = LoggerFactory.getLogger(LLMInputEnhancer.class);
    // --- WE ARE GOING LIVE ---
    private static final boolean DRY_RUN = false;

    private final ContextExtractor contextExtractor;
    private final OllamaClient ollamaClient;
    private final String modelName;

    public LLMInputEnhancer() {
        this.contextExtractor = new ContextExtractor();
        this.ollamaClient = new OllamaClient();
        String envModel = System.getenv("LLM_MODEL");
        if (envModel != null && !envModel.isEmpty()) {
            this.modelName = envModel;
        } else {
            this.modelName = "qwen2.5:7b"; // Sticking with our winning model
            logger.warn("LLM_MODEL environment variable not set. Using default model: {}", this.modelName);
        }
        logger.info("EvaluationLogger initialized. Results will be saved to a llm_eval_run_... .csv file.");
        logger.info("LLMInputEnhancer initialized. DRY_RUN is: {}. Using LLM Model: {}", DRY_RUN, this.modelName);
    }

    public void enhanceCandidate(TestChromosome candidate) {
        logger.info("--- Starting BATCH enhancement process for candidate ID: {} ---", candidate.getTestCase().getID());
        List<ParameterContext> contexts = contextExtractor.extractContexts(candidate);

        if (contexts.isEmpty()) { return; }

        String jsonRequest = formatBatchRequestForOllama(contexts, this.modelName);
        PromptLogger.log(jsonRequest);

        long startTime = System.currentTimeMillis();
        String ollamaResponse = ollamaClient.generate(jsonRequest);
        long duration = System.currentTimeMillis() - startTime;

        if (ollamaResponse != null) {
            try {
                JSONObject responseJson = new JSONObject(ollamaResponse);
                String innerJsonResponse = responseJson.getString("response");
                JSONObject innerJson = new JSONObject(innerJsonResponse);
                JSONArray suggestionsArray = innerJson.getJSONArray("suggestions");

                if (suggestionsArray.length() == contexts.size()) {
                    for (int i = 0; i < suggestionsArray.length(); i++) {
                        ParameterContext context = contexts.get(i);
                        JSONObject suggestion = suggestionsArray.getJSONObject(i);
                        String reasoning = suggestion.optString("reasoning_for_change_or_keep", "N/A");
                        String suggestedValue = suggestion.optString("suggested_value_as_string", "ERROR");
                        String confidence = suggestion.optString("confidence_low_medium_high", "N/A");

                        EvaluationLogger.log(String.valueOf(candidate.getTestCase().getID()), i, context.getMethodName(), context.getParameterJavaType(), context.getInitialValue(), suggestedValue, confidence, reasoning, "SUCCESS", duration / contexts.size());

                        if (DRY_RUN) {
                            logger.info("[DRY RUN] No changes applied for param {}.", context.getParameterName());
                        } else {
                            if (!suggestedValue.equals("ERROR") && confidence.equalsIgnoreCase("high")) {
                                logger.info("[LIVE RUN] Applying suggestion '{}' for param {}.", suggestedValue, context.getParameterName());
                                applySuggestion(context, candidate.getTestCase(), suggestedValue);
                            } else {
                                logger.warn("[LIVE RUN] Skipping suggestion for param {} due to error or low confidence ('{}').", context.getParameterName(), confidence);
                            }
                        }
                    }
                } else {
                     logger.error("Mismatch between contexts ({}) and suggestions ({}).", contexts.size(), suggestionsArray.length());
                     logFailure(candidate.getTestCase().getID(), contexts, "Mismatched suggestion count", duration);
                }
            } catch (Exception e) {
                 logger.error("Failed to parse JSON response from LLM: {}", e.getMessage());
                 logFailure(candidate.getTestCase().getID(), contexts, "JSON_PARSE_FAIL: " + e.getMessage(), duration);
            }
        } else {
            logger.error("No response from LLM for the entire batch.");
            logFailure(candidate.getTestCase().getID(), contexts, "REQUEST_FAIL: Ollama client returned null", duration);
        }
    }

    private void applySuggestion(ParameterContext context, TestCase testCase, String suggestedValue) {
        try {
            TestFactory factory = TestFactory.getInstance();
            String paramType = context.getParameterJavaType();
            VariableReference newParamRef = null;
            int position = testCase.size();

            if (context.getPossibleEnumValues() != null && !context.getPossibleEnumValues().isEmpty()) {
                Class<?>[] paramClasses = context.getOriginalStatement().getMethod().getMethod().getParameterTypes();
                Class<?> enumClass = paramClasses[context.getParameterIndex()];
                
                @SuppressWarnings({"unchecked", "rawtypes"})
                Enum<?> enumValue = Enum.valueOf((Class<Enum>) enumClass, suggestedValue);
                
                @SuppressWarnings({"unchecked", "rawtypes"})
                EnumPrimitiveStatement statement = new EnumPrimitiveStatement(testCase, enumValue);
                newParamRef = testCase.addStatement(statement, position);

            } else if (paramType.equals("int")) {
                IntPrimitiveStatement statement = new IntPrimitiveStatement(testCase, Integer.parseInt(suggestedValue));
                newParamRef = testCase.addStatement(statement, position);
            } else if (paramType.equals("double")) {
                DoublePrimitiveStatement statement = new DoublePrimitiveStatement(testCase, Double.parseDouble(suggestedValue));
                newParamRef = testCase.addStatement(statement, position);
            } else if (paramType.equals("java.lang.String")) {
                StringPrimitiveStatement statement = new StringPrimitiveStatement(testCase, suggestedValue);
                newParamRef = testCase.addStatement(statement, position);
            
            } else if (paramType.equals("custom_classes.Id")) {
                IntPrimitiveStatement intStmt = new IntPrimitiveStatement(testCase, Integer.parseInt(suggestedValue));
                VariableReference intRef = testCase.addStatement(intStmt, position++);
                Class<?> idClass = Class.forName(paramType);
                Constructor<?> idConstructor = idClass.getConstructor(int.class);
                GenericConstructor genericIdConstructor = new GenericConstructor(idConstructor, new GenericClass(idClass));

                // --- CORRECTED LINE ---
                List<VariableReference> constructorParams = new ArrayList<>();
                constructorParams.add(intRef);
                ConstructorStatement constructorStmt = new ConstructorStatement(testCase, genericIdConstructor, constructorParams);
                newParamRef = testCase.addStatement(constructorStmt, position);

            } else if (paramType.equals("custom_classes.Amount")) {
                IntPrimitiveStatement intStmt = new IntPrimitiveStatement(testCase, Integer.parseInt(suggestedValue));
                VariableReference intRef = testCase.addStatement(intStmt, position++);
                Class<?> amountClass = Class.forName(paramType);
                Constructor<?> amountConstructor = amountClass.getConstructor(int.class);
                GenericConstructor genericAmountConstructor = new GenericConstructor(amountConstructor, new GenericClass(amountClass));

                // --- CORRECTED LINE ---
                List<VariableReference> constructorParams = new ArrayList<>();
                constructorParams.add(intRef);
                ConstructorStatement constructorStmt = new ConstructorStatement(testCase, genericAmountConstructor, constructorParams);
                newParamRef = testCase.addStatement(constructorStmt, position);
            }

            if (newParamRef != null) {
                MethodStatement originalMethod = context.getOriginalStatement();
                originalMethod.getParameterReferences().set(context.getParameterIndex(), newParamRef);
                logger.info("Successfully replaced parameter {} in method {}.", context.getParameterName(), context.getMethodName());
            } else {
                logger.warn("Unhandled parameter type '{}'. No changes applied.", paramType);
            }

        } catch (Exception e) {
            logger.error("Failed to apply suggestion '{}' for parameter {}. Error: {}. Reverting to original value.",
                    suggestedValue, context.getParameterName(), e.getMessage(), e);
        }
    }

     private String formatBatchRequestForOllama(List<ParameterContext> contexts, String modelToUse) {
        JSONArray contextsArray = new JSONArray();
        for (ParameterContext context : contexts) {
            JSONObject contextJson = new JSONObject();
            contextJson.put("method_name", context.getMethodName());
            contextJson.put("parameter_java_type", context.getParameterJavaType());
            if (context.getPossibleEnumValues() != null && !context.getPossibleEnumValues().isEmpty()) {
                contextJson.put("possible_enum_values", new JSONArray(context.getPossibleEnumValues()));
            }
            contextsArray.put(contextJson);
        }

        String prompt = "### ROLE ###\nYou are an AI assistant that generates test data. Your output MUST be ONLY a valid JSON object matching the schema.\n\n"
                      + "### PARAMETER CONTEXTS ARRAY ###\n" + contextsArray.toString(2) + "\n\n"
                      + "### TASK ###\nBased ONLY on the context for EACH item in the array, generate a plausible value. CRITICAL RULE: If `possible_enum_values` exists, your suggestion MUST be from that list. Provide your response as a JSON object with a single key 'suggestions' holding an array of JSON objects with this exact schema: { \"reasoning_for_change_or_keep\": \"string\", \"suggested_value_as_string\": \"string\", \"confidence_low_medium_high\": \"string\" }";

        JSONObject finalRequest = new JSONObject();
        finalRequest.put("model", modelToUse);
        finalRequest.put("format", "json");
        finalRequest.put("stream", false);
        finalRequest.put("prompt", prompt);

        return finalRequest.toString();
    }
    
    private void logFailure(int candidateId, List<ParameterContext> contexts, String reason, long duration) {
        for(int i = 0; i < contexts.size(); i++) {
            ParameterContext context = contexts.get(i);
            EvaluationLogger.log(
                String.valueOf(candidateId), i, context.getMethodName(), context.getParameterJavaType(),
                context.getInitialValue(), "REQUEST_FAIL", "N/A", reason, "REQUEST_FAIL", duration
            );
        }
    }
}