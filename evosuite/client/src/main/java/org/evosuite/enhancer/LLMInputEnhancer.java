package org.evosuite.enhancer;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LLMInputEnhancer {

    private static final Logger logger = LoggerFactory.getLogger(LLMInputEnhancer.class);
    private static final boolean DRY_RUN = false;

    private boolean isCurrentlyModifying = false;

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
            this.modelName = "qwen2.5:7b";
            logger.warn("LLM_MODEL environment variable not set. Using default model: {}", this.modelName);
        }
        DebugStoryLogger.log("LLMInputEnhancer Initialized. DRY_RUN is: " + DRY_RUN + ". Model: " + this.modelName);
    }

    public void enhanceCandidate(TestChromosome candidate) {
        if (isCurrentlyModifying) {
            logger.warn("Re-entrant call to enhanceCandidate detected. Aborting to prevent infinite loop.");
            return;
        }
        this.isCurrentlyModifying = true;

        try {
            int candidateId = candidate.getTestCase().getID();
            List<VariableUsageContext> contexts = contextExtractor.extractContexts(candidate);

            if (contexts.isEmpty()) {
                return;
            }

            DebugStoryLogger.logCandidateStart(candidateId, contexts.size());
            DebugStoryLogger.logInitialTestCase(candidate.getTestCase().toCode());

            List<Modification> modificationPlan = new ArrayList<>();

            for (int i = 0; i < contexts.size(); i++) {
                VariableUsageContext context = contexts.get(i);
                DebugStoryLogger.logParamHeader(i + 1, contexts.size(), context.getSemanticVariable().getName(),
                        context.getSemanticVariableType(), context.getOriginalPrimitiveValue());

                String jsonRequest = formatRequestForOllama(context);
                PromptLogger.log(jsonRequest);
                DebugStoryLogger.logPrompt(jsonRequest);

                long startTime = System.currentTimeMillis();
                String ollamaResponse = ollamaClient.generate(jsonRequest);
                long duration = System.currentTimeMillis() - startTime;

                try {
                    JSONObject fullResponseJson = new JSONObject(ollamaResponse);
                    if (fullResponseJson.has("context")) {
                        fullResponseJson.remove("context");
                    }
                    DebugStoryLogger.logResponse(fullResponseJson.toString(2), duration);
                    JSONObject suggestion = new JSONObject(fullResponseJson.getString("response"));
                    DebugStoryLogger.logLLMSuggestion(suggestion.toString(2));

                    String suggestedValue = suggestion.optString("suggested_value_as_string", "ERROR");
                    if (!suggestedValue.equals(context.getOriginalPrimitiveValue())
                            && !suggestedValue.equals("ERROR")) {
                        DebugStoryLogger.logDecision("LLM suggested a new value. Adding to surgical plan.");
                        modificationPlan.add(new Modification(context, suggestedValue));
                    } else {
                        DebugStoryLogger.logDecision("LLM suggested keeping the original value. No change needed.");
                    }
                } catch (Exception e) {
                    DebugStoryLogger.logApplicationOutcome(false, "Failed to parse JSON response from LLM.", e);
                }
            }

            if (!DRY_RUN && !modificationPlan.isEmpty()) {
                executeSurgicalPlan(candidate.getTestCase(), modificationPlan);
            } else if (DRY_RUN) {
                DebugStoryLogger.log("\n[DRY RUN] Surgical plan created but not executed. Total modifications planned: "
                        + modificationPlan.size());
            }

            DebugStoryLogger.logFinalTestCase(candidate.getTestCase().toCode());
            DebugStoryLogger.logCandidateEnd(candidateId, modificationPlan.size(), 0, 0); // TODO: Fix counters

        } finally {
            this.isCurrentlyModifying = false;
        }
    }

    private void executeSurgicalPlan(TestCase testCase, List<Modification> plan) {
        DebugStoryLogger.log("\n--- [PHASE 2] EXECUTING SURGICAL PLAN ---");
        int appliedCount = 0;
        int failedCount = 0;

        for (int i = 0; i < plan.size(); i++) {
            Modification mod = plan.get(i);
            DebugStoryLogger.log(String.format("\n--- Applying Mod %d/%d for var '%s' ---", i + 1, plan.size(),
                    mod.context.getSemanticVariable().getName()));
            boolean success = applySingleModification(mod.context, testCase, mod.suggestedValue);
            if (success) {
                appliedCount++;
            } else {
                failedCount++;
            }
        }

        cleanupDeadStatements(testCase);

        DebugStoryLogger.log(String.format("\n--- SURGICAL EXECUTION COMPLETE: Applied: %d | Failed: %d ---",
                appliedCount, failedCount));
    }

   @SuppressWarnings("rawtypes")
    private boolean applySingleModification(VariableUsageContext context, TestCase testCase, String suggestedValue) {
        Statement originalDeclaration = context.getDeclarationStatement();
        DebugStoryLogger.logApplicationAction("Executing surgery for " + context.getSemanticVariable().getName() + " based on declaration: " + originalDeclaration.getCode());

        // --- THE FINAL UNIFIED LOGIC ---
        // Case 1: The target is a simple primitive enum. Safe to use setValue().
        if (originalDeclaration instanceof EnumPrimitiveStatement) {
            DebugStoryLogger.logDecision("Declaration is a simple EnumPrimitive. Using setValue() surgery.");
            try {
                PrimitiveStatement primStmt = (PrimitiveStatement) originalDeclaration;
                Object newValue = parseValue(suggestedValue, primStmt.getValue().getClass());
                primStmt.setValue(newValue);
                DebugStoryLogger.logApplicationOutcome(true, "SUCCESS: setValue() called on: " + primStmt.getReturnValue().getName(), null);
                return true;
            } catch (Exception e) {
                DebugStoryLogger.logApplicationOutcome(false, "FAILED: Exception during setValue()", e);
                return false;
            }
        }
        // Case 2: The target is a refactored class from a factory. Must use "Smart Duplication".
        else if (originalDeclaration instanceof MethodStatement) {
            DebugStoryLogger.logDecision("Declaration is a factory call. Using 'Smart Duplication' surgery.");
            MethodStatement methodStmt = (MethodStatement) originalDeclaration;
            String methodName = methodStmt.getMethodName();

            if (methodName.equals("fromString")) {
                try {
                    VariableReference oldVar = context.getSemanticVariable();
                    int position = originalDeclaration.getPosition();
    
                    StringPrimitiveStatement newPrimitiveStmt = new StringPrimitiveStatement(testCase, suggestedValue);
                    testCase.addStatement(newPrimitiveStmt, position);
                    
                    // Re-wire the original declaration to use the new primitive
                    ((MethodStatement) originalDeclaration).replaceParameterReference(newPrimitiveStmt.getReturnValue(), 0);
    
                    DebugStoryLogger.logApplicationOutcome(true, "SUCCESS: Re-wired declaration of '" + oldVar.getName() + "' to use new primitive.", null);
                    return true;
                } catch (Exception e) {
                    DebugStoryLogger.logApplicationOutcome(false, "FAILED: Exception during statement replacement surgery.", e);
                    return false;
                }
            } 
            DebugStoryLogger.logApplicationOutcome(false, "FAILED: Unsupported factory method: " + methodName, null);
            return false;

        } else {
            DebugStoryLogger.logApplicationOutcome(false, "FAILED: Unhandled declaration type: " + originalDeclaration.getClass().getSimpleName(), null);
            return false;
        }
    }

    private Object parseValue(String value, Class<?> type) {
        if (type.isEnum()) {
            @SuppressWarnings({ "unchecked" })
            Enum<?> enumValue = Enum.valueOf((Class<Enum>) type, value);
            return enumValue;
        }
        // ... add other types if needed ...
        return value;
    }

    private String formatRequestForOllama(VariableUsageContext context) {
        JSONObject contextJson = new JSONObject();
        contextJson.put("variable_type", context.getSemanticVariableType());
        contextJson.put("initial_value", context.getOriginalPrimitiveValue());
        contextJson.put("usage_in_methods", new JSONArray(context.getUsageMethodNames()));

        List<String> preferredValues = new ArrayList<>();

        Statement declaration = context.getDeclarationStatement();
        if (declaration instanceof EnumPrimitiveStatement) {
            @SuppressWarnings("rawtypes")
            EnumPrimitiveStatement enumStmt = (EnumPrimitiveStatement) declaration;
            contextJson.put("possible_enum_values", new JSONArray(enumStmt.getEnumValues()));
        } else {
            try {
                Class<?> targetClass = context.getSemanticVariable().getVariableClass();
                Field examplesField = targetClass.getField("examples");
                // Check if it's a public static final String[]
                int modifiers = examplesField.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && examplesField.getType().equals(String[].class)) {
                    String[] examples = (String[]) examplesField.get(null); // Get the static array
                    preferredValues.addAll(Arrays.asList(examples));
                }
            } catch (NoSuchFieldException e) {
                // This is fine, it just means the class doesn't have an examples field.
            } catch (Exception e) {
                logger.warn("Reflection failed while trying to find 'examples' field for {}: {}",
                        context.getSemanticVariableType(), e.getMessage());
            }
        }

        if (!preferredValues.isEmpty()) {
            contextJson.put("preferred_values", new JSONArray(preferredValues));
        }

        String taskInstruction = "Analyze the variable. Your goal is to suggest ONE SINGLE replacement value for `initial_value`. If the `initial_value` is semantically poor (e.g., a negative ID, an unrealistic amount), suggest a better, single value. If the `initial_value` is already plausible, you can suggest a different single value for test diversity, or suggest keeping the original value. The `suggested_value_as_string` MUST be a single, simple value that can be directly parsed into the variable's type. It must NOT contain colons, commas, or multiple assignments.";
        if (contextJson.has("possible_enum_values") || contextJson.has("preferred_values")) {
            taskInstruction += " CRITICAL RULE: If `possible_enum_values` or `preferred_values` exists, your suggestion should be semantically similar to those examples. For enums, you MUST pick one from the list. For the `preferred_values`, use it as a guide to suggest a plausible value, please refrain from reusing the same value as this will make you a glorified random choice engine.";
        }

        String prompt = "### ROLE ###\nYou are an AI test data generator. Your task is to analyze a single declared variable and its usages, then decide if its initial value should be changed.\n\n"
                + "### VARIABLE CONTEXT ###\n" + contextJson.toString(2) + "\n\n"
                + "### TASK ###\n" + taskInstruction
                + " Provide your response as a single JSON object with this exact schema: { \"reasoning_for_change_or_keep\": \"string\", \"suggested_value_as_string\": \"string\", \"confidence_low_medium_high\": \"string\" }";

        JSONObject finalRequest = new JSONObject();
        finalRequest.put("model", this.modelName);
        finalRequest.put("format", "json");
        finalRequest.put("stream", false);
        finalRequest.put("prompt", prompt);

        return finalRequest.toString();
    }

    /**
     * The "Cleanup Crew". This method iterates backwards through the test case
     * and removes any statements whose return values are no longer used by any
     * subsequent statements.
     */
    private void cleanupDeadStatements(TestCase testCase) {
        DebugStoryLogger.log("\n--- [PHASE 3] CLEANING UP DEAD STATEMENTS ---");
        int removedCount = 0;

        // We iterate backwards to ensure that removing a statement does not
        // mess up the indices of the statements we still need to check.
        for (int i = testCase.size() - 1; i >= 0; i--) {
            Statement currentStatement = testCase.getStatement(i);
            VariableReference var = currentStatement.getReturnValue();

            // We only consider cleaning up variable declarations, not action calls
            // that might have important side effects even if their return value isn't used.
            if (!(currentStatement instanceof PrimitiveStatement || currentStatement instanceof ConstructorStatement)) {
                continue;
            }

            // Check if this variable is used by any statement that comes AFTER it.
            // The hasReferences() method checks the entire test case, which is what we need here.
            if (!testCase.hasReferences(var)) {
                DebugStoryLogger.log("Removing dead statement at position " + i + ": " + currentStatement.getCode());
                testCase.remove(i);
                removedCount++;
            }
        }
        DebugStoryLogger.log("Cleanup complete. Removed " + removedCount + " dead statements.");
    }
}