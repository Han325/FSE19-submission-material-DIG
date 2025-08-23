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
    private final LLMClient llmClient;
    private final String modelName;

    public LLMInputEnhancer() {
        this.contextExtractor = new ContextExtractor();

        String provider = "ollama";
        if (provider == null || provider.trim().isEmpty()) {
            provider = "ollama";
            logger.warn("LLM_PROVIDER environment variable not set. Defaulting to '{}'", provider);
        }

        switch (provider.toLowerCase()) {
            case "openai":
                this.llmClient = new OpenAIClient();
                logger.info("Using OpenAI client.");
                break;
            case "ollama":
            default:
                this.llmClient = new OllamaClient();
                logger.info("Using Ollama client.");
                break;
        }

        String envModel = System.getenv("LLM_MODEL");
        if (envModel != null && !envModel.isEmpty()) {
            this.modelName = envModel;
        } else {
            if ("openai".equalsIgnoreCase(provider)) {
                this.modelName = "gpt-4o-mini"; 
            } else {
                this.modelName = "qwen2.5:7b";
            }
            logger.warn("LLM_MODEL environment variable not set. Using default model for provider '{}': {}", provider, this.modelName);
        }
        DebugStoryLogger.log("LLMInputEnhancer Initialized. Provider: " + provider.toUpperCase() + ". Model: " + this.modelName + ". DRY_RUN is: " + DRY_RUN);
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

                String jsonRequest = createLlmRequestPayload(context);
                PromptLogger.log(jsonRequest);
                DebugStoryLogger.logPrompt(jsonRequest);

                long startTime = System.currentTimeMillis();
                String llmResponse = llmClient.generate(jsonRequest);
                long duration = System.currentTimeMillis() - startTime;

                try {
                    JSONObject fullResponseJson = new JSONObject(llmResponse);
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
        DebugStoryLogger.logApplicationAction("Executing surgery for " + context.getSemanticVariable().getName()
                + " based on declaration: " + originalDeclaration.getCode());

        // --- THE FINAL UNIFIED LOGIC ---
        // Case 1: The target is a simple primitive enum. Safe to use setValue().
        if (originalDeclaration instanceof EnumPrimitiveStatement) {
            DebugStoryLogger.logDecision("Declaration is a simple EnumPrimitive. Using setValue() surgery.");
            try {
                PrimitiveStatement primStmt = (PrimitiveStatement) originalDeclaration;
                Object newValue = parseValue(suggestedValue, primStmt.getValue().getClass());
                primStmt.setValue(newValue);
                DebugStoryLogger.logApplicationOutcome(true,
                        "SUCCESS: setValue() called on: " + primStmt.getReturnValue().getName(), null);
                return true;
            } catch (Exception e) {
                DebugStoryLogger.logApplicationOutcome(false, "FAILED: Exception during setValue()", e);
                return false;
            }
        }
        // Case 2: The target is a refactored class from a factory. Must use "Smart
        // Duplication".
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
                    ((MethodStatement) originalDeclaration).replaceParameterReference(newPrimitiveStmt.getReturnValue(),
                            0);

                    DebugStoryLogger.logApplicationOutcome(true,
                            "SUCCESS: Re-wired declaration of '" + oldVar.getName() + "' to use new primitive.", null);
                    return true;
                } catch (Exception e) {
                    DebugStoryLogger.logApplicationOutcome(false,
                            "FAILED: Exception during statement replacement surgery.", e);
                    return false;
                }
            }
            DebugStoryLogger.logApplicationOutcome(false, "FAILED: Unsupported factory method: " + methodName, null);
            return false;

        } else {
            DebugStoryLogger.logApplicationOutcome(false,
                    "FAILED: Unhandled declaration type: " + originalDeclaration.getClass().getSimpleName(), null);
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

    
    private String createLlmRequestPayload(VariableUsageContext context) {
        JSONObject contextJson = new JSONObject();
        contextJson.put("variable_type", context.getSemanticVariableType());
        contextJson.put("initial_value", context.getOriginalPrimitiveValue());
        contextJson.put("usage_in_methods", new JSONArray(context.getUsageMethodNames()));

        // =================================================================
        // ========= CHANGE START: New Prompt Generation Logic =============
        // =================================================================

        String promptInstructions; // This will hold our chosen prompt text

        Statement declaration = context.getDeclarationStatement();
        if (declaration instanceof EnumPrimitiveStatement) {
            // --- STRATEGY FOR ENUMS ---
            // For enums, the goal is to pick a *different* valid option to increase
            // diversity.
            @SuppressWarnings("rawtypes")
            EnumPrimitiveStatement enumStmt = (EnumPrimitiveStatement) declaration;
            contextJson.put("possible_enum_values", new JSONArray(enumStmt.getEnumValues()));

            promptInstructions = "You MUST follow these steps:\n\n"
                    + "**Step 1: ANALYZE THE CONTEXT**\n"
                    + "Analyze the `initial_value` and the `possible_enum_values` list.\n\n"
                    + "**Step 2: CHOOSE A DIVERSE VALUE**\n"
                    + "Your goal is to maximize test diversity. Your strategy is to select a **DIFFERENT** value from the `possible_enum_values` list.\n"
                    + "- If the `initial_value` is already a valid option, you **MUST** choose another one from the list.\n"
                    + "- Do **NOT** suggest the same value back.\n\n"
                    + "**Step 3: GENERATE THE FINAL JSON OUTPUT**\n"
                    + "Provide your response as a single JSON object. The `suggested_value_as_string` **MUST** be one of the exact strings from the `possible_enum_values` list.";

        } else {
            // --- STRATEGY FOR STANDARD/CUSTOM TYPES (String, Amount, etc.) ---
            // This is the full "Cognitive Process Prompting" for handling garbage-in and
            // extrapolation.
            List<String> preferredValues = new ArrayList<>();
            try {
                Class<?> targetClass = context.getSemanticVariable().getVariableClass();
                Field examplesField = targetClass.getField("examples");
                int modifiers = examplesField.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
                        && examplesField.getType().equals(String[].class)) {
                    String[] examples = (String[]) examplesField.get(null);
                    preferredValues.addAll(Arrays.asList(examples));
                }
            } catch (NoSuchFieldException e) {
                // This is fine, it just means the class doesn't have an examples field.
            } catch (Exception e) {
                logger.warn("Reflection failed while trying to find 'examples' field for {}: {}",
                        context.getSemanticVariableType(), e.getMessage());
            }

            if (!preferredValues.isEmpty()) {
                contextJson.put("preferred_values", new JSONArray(preferredValues));
            }

            promptInstructions = "You MUST follow these steps in order:\n\n"
                    + "**Step 1: ASSESS THE INITIAL VALUE**\n"
                    + "First, perform a sanity check on the `initial_value`. Your goal is to determine if it is plausible or nonsensical garbage.\n"
                    + "- A **plausible** value has semantic meaning related to its `variable_type` (e.g., for a type `Email`, a value like `\"test@example.com\"` is plausible).\n"
                    + "- A **nonsensical** value is random machine-generated text (e.g., for a type `Email` or `Amount`, a value like `\"zY7sAdcSm\"` is nonsensical).\n\n"
                    + "**Step 2: REASON ABOUT THE GENERATION STRATEGY**\n"
                    + "Based on your assessment in Step 1 and the available context, choose your strategy:\n\n"
                    + "- **IF the `initial_value` is NONSENSICAL:** You **MUST** completely discard it. Your strategy is to generate a brand new, canonical, and realistic value from scratch based *only* on the `variable_type` and its `usage_in_methods`.\n\n"
                    + "- **IF the `initial_value` is PLAUSIBLE:** Your strategy is to create a *different* but equally plausible value to increase test diversity. Now, consider the `preferred_values`:\n"
                    + "    - **If `preferred_values` exists:** This list defines a semantic category. Your task is to **extrapolate** from this category. Generate a **NEW, ANALOGOUS** value that would logically fit in the same group but is **NOT** already present. Your goal is to expand semantic diversity. For example, if the category seems to be 'Programming Languages', and the list is `[\"Java\", \"Python\"]`, a good new suggestion would be `\"Rust\"` or `\"Go\"`.\n"
                    + "    - **If `preferred_values` does NOT exist:** Generate a new, plausible value based on the `variable_type` and `usage_in_methods` that is different from the `initial_value`.\n\n"
                    + "**Step 3: GENERATE THE FINAL JSON OUTPUT**\n"
                    + "Based on your reasoning in Step 2, provide your response as a single JSON object. The `suggested_value_as_string` MUST be a single, simple value that can be directly parsed. Do not include explanations, colons, or multiple assignments in the value itself.";
        }

        String prompt = "### ROLE ###\n"
                + "You are an expert Test Data Analyst. Your purpose is to generate semantically rich, realistic, and diverse data for software testing. You are a creative generator, not a random selector.\n\n"
                + "### VARIABLE CONTEXT ###\n" + contextJson.toString(2) + "\n\n"
                + "### INSTRUCTIONS & REASONING PROCESS ###\n" + promptInstructions + "\n\n"
                + "### OUTPUT SCHEMA ###\n"
                + "{ \"reasoning_for_change_or_keep\": \"string\", \"suggested_value_as_string\": \"string\", \"confidence_low_medium_high\": \"string\" }";

        // =================================================================
        // ========= CHANGE END: New Prompt Generation Logic ===============
        // =================================================================

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
            // The hasReferences() method checks the entire test case, which is what we need
            // here.
            if (!testCase.hasReferences(var)) {
                DebugStoryLogger.log("Removing dead statement at position " + i + ": " + currentStatement.getCode());
                testCase.remove(i);
                removedCount++;
            }
        }
        DebugStoryLogger.log("Cleanup complete. Removed " + removedCount + " dead statements.");
    }
}