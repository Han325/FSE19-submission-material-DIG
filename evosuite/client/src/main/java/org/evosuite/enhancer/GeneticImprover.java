package org.evosuite.enhancer;

import org.evosuite.enhancer.distance.InputVectorDistance;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Takes a list of LLM-enhanced seeds and generates a larger, more diverse
 * population using Genetic Improvement techniques.
 */
public class GeneticImprover {

    // --- MASTER SWITCHES ---
    private static final boolean DEEP_DIVE_MODE = false; // SET TO true FOR DETAILED LOGGING OF ONE CANDIDATE
    private static final boolean TRACE_MUTATION_ATTEMPTS = false; // SET TO true FOR ULTRA-DETAILED MUTATION TRACING
    private static final boolean DRY_RUN = false; // Master switch for GI phase

    private static final int AUDITION_POOL_SIZE = 50; // Number of random mutations to generate per seed
    private static final int MAX_AUDITION_ATTEMPTS = 100; // Prevents infinite loops
    private static final int MAX_MUTATIONS_PER_CANDIDATE = 3; // NEW: Controls mutation budget

    private static class MutationResult {
        final TestChromosome chromosome;
        final String description;

        MutationResult(TestChromosome chromosome, String description) {
            this.chromosome = chromosome;
            this.description = description;
        }
    }

    public GeneticImprover() {
    }

    public List<TestChromosome> diversifyPopulation(List<TestChromosome> seeds, int variationsPerSeed) {
        if (DEEP_DIVE_MODE) {
            runDeepDive(seeds);
            return seeds;
        }

        if (DRY_RUN) {
            DebugStoryLogger.logGIPhaseStart(seeds.size(), variationsPerSeed, true);
            return seeds;
        }

        DebugStoryLogger.logGIPhaseStart(seeds.size(), variationsPerSeed, false);
        List<TestChromosome> superchargedPopulation = new ArrayList<>();

        for (int i = 0; i < seeds.size(); i++) {
            TestChromosome seed = seeds.get(i);
            DebugStoryLogger.logGISeedProcessingStart(seed.getTestCase().getID(), i + 1, seeds.size());

            ContextExtractor extractor = new ContextExtractor();
            List<VariableUsageContext> semanticContexts = extractor.extractContexts(seed);

            if (semanticContexts.isEmpty()) {
                DebugStoryLogger.log("GI WARNING: Seed #" + seed.getTestCase().getID()
                        + " has no semantic variables to mutate. Passing original seed through UNMUTATED.");
                superchargedPopulation.add(seed);
                continue;
            }

            List<TestChromosome> diverseFamily = selectDiverseFamily(seed, variationsPerSeed, semanticContexts);
            superchargedPopulation.addAll(diverseFamily);
        }

        DebugStoryLogger.logGIPhaseEnd(superchargedPopulation.size());
        return superchargedPopulation;
    }

    // --- HELPER FOR EFFICIENT VECTOR GENERATION ---
    private Map<String, PrimitiveStatement<?>> getLiveSemanticPrimitiveVector(TestChromosome chromosome) {
        Map<String, PrimitiveStatement<?>> vector = new HashMap<>();
        ContextExtractor extractor = new ContextExtractor();
        List<VariableUsageContext> liveContexts = extractor.extractContexts(chromosome);
        for (VariableUsageContext liveContext : liveContexts) {
            PrimitiveStatement<?> rootPrimitive = findRootPrimitive(chromosome.getTestCase(),
                    liveContext.getDeclarationStatement());
            if (rootPrimitive != null) {
                vector.put(liveContext.getSemanticVariable().getName(), rootPrimitive);
            }
        }
        return vector;
    }

    private void runDeepDive(List<TestChromosome> seeds) {
        if (seeds.isEmpty()) {
            DebugStoryLogger.log("DEEP DIVE: Seed list is empty. Aborting.");
            return;
        }
        TestChromosome targetSeed = null;
        List<VariableUsageContext> targetContexts = new ArrayList<>();
        int maxPrimitives = -1;
        ContextExtractor extractor = new ContextExtractor();
        for (TestChromosome seed : seeds) {
            List<VariableUsageContext> contexts = extractor.extractContexts(seed);
            if (contexts.size() > maxPrimitives) {
                maxPrimitives = contexts.size();
                targetSeed = seed;
                targetContexts = contexts;
            }
        }
        if (targetSeed == null) {
            DebugStoryLogger.log("DEEP DIVE: No seeds with mutable primitives found. Aborting.");
            return;
        }
        DebugStoryLogger.log("\n==================== [DEEP DIVE MODE] ====================");
        DebugStoryLogger.log("Selected seed #" + targetSeed.getTestCase().getID() + " for deep dive (has "
                + maxPrimitives + " semantic primitives).");
        DebugStoryLogger.log("--- DEEP DIVE: Initial Seed State (Used as mutation base) ---");
        DebugStoryLogger.log(targetSeed.getTestCase().toCode());
        List<MutationResult> auditionPool = createAuditionPool(targetSeed, targetContexts);
        DebugStoryLogger.log("\n--- DEEP DIVE: Audition Pool (" + auditionPool.size() + " candidates) ---");
        for (int i = 0; i < auditionPool.size(); i++) {
            MutationResult result = auditionPool.get(i);
            DebugStoryLogger.log(String.format("--- Auditioner #%d: %s ---", i + 1, result.description));
        }
        DebugStoryLogger.log("\n--- DEEP DIVE: Maximin Selection Process ---");

        // --- START OF PERFORMANCE & LOGIC FIX FOR DEEP DIVE ---
        Map<TestChromosome, Map<String, PrimitiveStatement<?>>> vectorCache = new HashMap<>();
        vectorCache.put(targetSeed, getLiveSemanticPrimitiveVector(targetSeed));
        for (MutationResult result : auditionPool) {
            vectorCache.put(result.chromosome, getLiveSemanticPrimitiveVector(result.chromosome));
        }

        List<TestChromosome> diverseFamily = new ArrayList<>();
        InputVectorDistance distanceCalculator = new InputVectorDistance();

        for (int i = 0; i < 5; i++) {
            DebugStoryLogger.log(String.format("\n--- Selecting Family Member #%d ---", i + 1));
            MutationResult bestResult = null;
            double maxMinDistance = -1.0;
            for (MutationResult result : auditionPool) {
                DebugStoryLogger.log("  - Evaluating Auditioner: " + result.description);
                double minDistanceToFamily = Double.MAX_VALUE;
                Map<String, PrimitiveStatement<?>> resultVector = vectorCache.get(result.chromosome);

                if (diverseFamily.isEmpty()) {
                    minDistanceToFamily = distanceCalculator.calculate(resultVector, vectorCache.get(targetSeed));
                } else {
                    for (TestChromosome familyMember : diverseFamily) {
                        double dist = distanceCalculator.calculate(resultVector, vectorCache.get(familyMember));
                        DebugStoryLogger.log(String.format("    - Distance to family member #%d: %.4f",
                                familyMember.getTestCase().getID(), dist));
                        if (dist < minDistanceToFamily) {
                            minDistanceToFamily = dist;
                        }
                    }
                }
                DebugStoryLogger.log(String.format("  - Min-Distance for this auditioner: %.4f", minDistanceToFamily));
                if (minDistanceToFamily > maxMinDistance) {
                    maxMinDistance = minDistanceToFamily;
                    bestResult = result;
                }
            }
            if (bestResult != null) {
                DebugStoryLogger.log(String.format(
                        "WINNER: Chosen candidate with mutation '%s'. Max-Min-Distance: %.4f. Adding to family.",
                        bestResult.description, maxMinDistance));
                diverseFamily.add(bestResult.chromosome);
                auditionPool.remove(bestResult);
            } else {
                DebugStoryLogger.log("WINNER: Could not find any more diverse candidates.");
                break;
            }
        }
        // --- END OF FIX ---

        DebugStoryLogger.log("\n--- DEEP DIVE: Final Diverse Family ---");
        for (int i = 0; i < diverseFamily.size(); i++) {
            DebugStoryLogger.log(String.format("--- Final Family Member #%d ---", i + 1));
            DebugStoryLogger.log(diverseFamily.get(i).getTestCase().toCode());
        }
        DebugStoryLogger.log("==================== [DEEP DIVE COMPLETE] ====================");
    }

    private List<TestChromosome> selectDiverseFamily(TestChromosome seed, int familySize,
            List<VariableUsageContext> semanticContexts) {
        // --- START OF PERFORMANCE & LOGIC FIX FOR PRODUCTION ---
        List<MutationResult> auditionPool = createAuditionPool(seed, semanticContexts);

        Map<TestChromosome, Map<String, PrimitiveStatement<?>>> vectorCache = new HashMap<>();
        vectorCache.put(seed, getLiveSemanticPrimitiveVector(seed));
        for (MutationResult result : auditionPool) {
            vectorCache.put(result.chromosome, getLiveSemanticPrimitiveVector(result.chromosome));
        }

        List<TestChromosome> diverseFamily = new ArrayList<>();
        InputVectorDistance distanceCalculator = new InputVectorDistance();

        while (diverseFamily.size() < familySize && !auditionPool.isEmpty()) {
            MutationResult bestResult = null;
            double maxMinDistance = -1.0;
            for (MutationResult result : auditionPool) {
                double minDistanceToFamily = Double.MAX_VALUE;
                Map<String, PrimitiveStatement<?>> resultVector = vectorCache.get(result.chromosome);

                if (diverseFamily.isEmpty()) {
                    minDistanceToFamily = distanceCalculator.calculate(resultVector, vectorCache.get(seed));
                } else {
                    for (TestChromosome familyMember : diverseFamily) {
                        double dist = distanceCalculator.calculate(resultVector, vectorCache.get(familyMember));
                        if (dist < minDistanceToFamily) {
                            minDistanceToFamily = dist;
                        }
                    }
                }
                if (minDistanceToFamily > maxMinDistance) {
                    maxMinDistance = minDistanceToFamily;
                    bestResult = result;
                }
            }
            if (bestResult != null) {
                DebugStoryLogger.logGISelection(diverseFamily.size() + 1, familySize, bestResult.description,
                        maxMinDistance);
                diverseFamily.add(bestResult.chromosome);
                auditionPool.remove(bestResult);
            } else {
                DebugStoryLogger.log(
                        "GI WARNING: Could not find any more diverse candidates for this seed. Family size may be smaller than requested.");
                break;
            }
        }
        // --- END OF FIX ---
        return diverseFamily;
    }

    // =================================================================
// THIS IS THE DIAGNOSTIC VERSION OF `createAuditionPool`
// WITH TEMPORARY LOGGING TO PROVE THE HYPOTHESIS.
// =================================================================
    private List<MutationResult> createAuditionPool(TestChromosome seed, List<VariableUsageContext> initialContexts) {
        List<MutationResult> pool = new ArrayList<>();
        Set<String> seenChromosomes = new HashSet<>();
        seenChromosomes.add(seed.getTestCase().toCode());
        
        // We NEED the extractor for this diagnostic.
        ContextExtractor extractor = new ContextExtractor(); 

        List<VariableUsageContext> trulyMutableContexts = new ArrayList<>();
        for (VariableUsageContext context : initialContexts) {
            Statement declaration = seed.getTestCase().getStatement(context.getDeclarationStatement().getPosition());
            // We only add contexts whose declaration is a type our mutator can handle.
            if (declaration instanceof MethodStatement) {
                trulyMutableContexts.add(context);
            }
        }

        int attempts = 0;
        while (pool.size() < AUDITION_POOL_SIZE && attempts < MAX_AUDITION_ATTEMPTS) {
            attempts++;
            TestChromosome clone = (TestChromosome) seed.clone();
            
            List<VariableUsageContext> availableContexts = new ArrayList<>(initialContexts);
            Randomness.shuffle(availableContexts);
            
            int mutationBudget = Randomness.nextInt(MAX_MUTATIONS_PER_CANDIDATE) + 1;
            List<String> mutationDescriptions = new ArrayList<>();
            
            // --- START OF NEW DIAGNOSTIC LOGGING ---
            int contextsBeforeMutation = extractor.extractContexts(clone).size();
            DebugStoryLogger.trace(String.format("DIAGNOSTIC: Starting candidate attempt #%d with budget %d. Initial valid contexts: %d", attempts, mutationBudget, contextsBeforeMutation));
            // --- END OF NEW DIAGNOSTIC LOGGING ---

            int mutationsApplied = 0;
            for (VariableUsageContext contextToMutate : availableContexts) {
                if (mutationsApplied >= mutationBudget) break;

                String desc = applyRandomMutation(clone, Collections.singletonList(contextToMutate), attempts);

                if (desc != null) {
                    mutationDescriptions.add(desc);
                    mutationsApplied++;
                    
                    // --- START OF NEW DIAGNOSTIC LOGGING ---
                    int contextsAfterMutation = extractor.extractContexts(clone).size();
                    DebugStoryLogger.trace(String.format("    -> DIAGNOSTIC: Mutation %d SUCCESS. Context count changed from %d to %d.", mutationsApplied, contextsBeforeMutation, contextsAfterMutation));
                    contextsBeforeMutation = contextsAfterMutation; // Update for the next loop iteration
                    // --- END OF NEW DIAGNOSTIC LOGGING ---
                }
            }

            if (!mutationDescriptions.isEmpty()) {
                if (seenChromosomes.add(clone.getTestCase().toCode())) {
                    String finalDescription = String.format("%d mutations applied: %s", 
                                                            mutationDescriptions.size(), 
                                                            String.join(" | ", mutationDescriptions));
                    if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("createAuditionPool: New unique candidate found with " + mutationDescriptions.size() + " mutation(s). Adding to pool (Size: " + (pool.size() + 1) + ").");
                    pool.add(new MutationResult(clone, finalDescription));
                } else {
                    if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("createAuditionPool: Duplicate candidate generated after mutations. Discarding.");
                }
            }
        }
        return pool;
    }

    private String applyRandomMutation(TestChromosome chromosome, List<VariableUsageContext> semanticContexts,
            int attemptNum) {
        if (semanticContexts.isEmpty())
            return null;
        if (TRACE_MUTATION_ATTEMPTS)
            DebugStoryLogger.trace(String.format("\n// --- Attempt %d/%d ---", attemptNum, MAX_AUDITION_ATTEMPTS));
        List<VariableUsageContext> mutableContexts = new ArrayList<>();
        for (VariableUsageContext context : semanticContexts) {
            Statement s = chromosome.getTestCase().getStatement(context.getDeclarationStatement().getPosition());
            if (!(s instanceof EnumPrimitiveStatement)) {
                mutableContexts.add(context);
            }
        }

        // If only ENUMs were available, we can't mutate anything.
        if (mutableContexts.isEmpty()) {
            if (TRACE_MUTATION_ATTEMPTS)
                DebugStoryLogger.trace("applyRandomMutation: No non-ENUM variables available to mutate. Skipping.");
            return null;
        }
        VariableUsageContext targetContext = Randomness.choice(semanticContexts);
        if (TRACE_MUTATION_ATTEMPTS)
            DebugStoryLogger.trace(
                    "applyRandomMutation: Chose semantic context for '" + targetContext.getSemanticVariable().getName()
                            + "' (" + targetContext.getSemanticVariableType() + ").");
        Statement declarationStatement = chromosome.getTestCase()
                .getStatement(targetContext.getDeclarationStatement().getPosition());
        if (declarationStatement instanceof EnumPrimitiveStatement) {
            return null;
        }
        if (declarationStatement instanceof MethodStatement) {
            if (TRACE_MUTATION_ATTEMPTS)
                DebugStoryLogger.trace(
                        "applyRandomMutation: Declaration is a MethodStatement. Applying 'Smart Duplication' surgery...");
            return mutateBySmartDuplication(chromosome.getTestCase(), (MethodStatement) declarationStatement);
        }
        if (TRACE_MUTATION_ATTEMPTS)
            DebugStoryLogger.trace("applyRandomMutation: FAILED. Unhandled declaration type: "
                    + declarationStatement.getClass().getSimpleName());
        return null;
    }

    private String mutateBySmartDuplication(TestCase testCase, MethodStatement methodStmt) {
        PrimitiveStatement<?> originalPrimitive = findRootPrimitive(testCase, methodStmt);
        if (originalPrimitive == null) {
            if (TRACE_MUTATION_ATTEMPTS)
                DebugStoryLogger
                        .trace("mutateBySmartDuplication: FAILED. Could not trace back to a root primitive from "
                                + methodStmt.getCode());
            return null;
        }
        if (TRACE_MUTATION_ATTEMPTS)
            DebugStoryLogger.trace("mutateBySmartDuplication: Found root primitive: " + originalPrimitive.getCode());
        String newValue = generateMutatedValue(originalPrimitive);
        if (newValue == null || newValue.equals(originalPrimitive.getValue().toString())) {
            if (TRACE_MUTATION_ATTEMPTS)
                DebugStoryLogger.trace("mutateBySmartDuplication: FAILED. Mutation resulted in no change.");
            return null;
        }
        try {
            int position = methodStmt.getPosition();
            StringPrimitiveStatement newPrimitiveStmt = new StringPrimitiveStatement(testCase, newValue);
            testCase.addStatement(newPrimitiveStmt, position);
            methodStmt.replaceParameterReference(newPrimitiveStmt.getReturnValue(), 0);
            cleanupDeadStatements(testCase);
            String originalValue = originalPrimitive.getValue().toString();
            String varName = methodStmt.getReturnValue().getName();
            return String.format("Mutated '%s' via SMART_DUPLICATION from '%s' to '%s'", varName, originalValue,
                    newValue);
        } catch (Exception e) {
            if (TRACE_MUTATION_ATTEMPTS)
                DebugStoryLogger.trace("mutateBySmartDuplication: FAILED during surgery. " + e.getMessage());
            return null;
        }
    }

    private String generateMutatedValue(PrimitiveStatement<?> primitive) {
        if (primitive instanceof StringPrimitiveStatement) {
            String value = (String) primitive.getValue();
            if (isNumeric(value)) {
                return mutateNumericStringValue(value);
            } else {
                return mutateAlphanumericStringValue(value);
            }
        }
        return null;
    }

    private PrimitiveStatement<?> findRootPrimitive(TestCase testCase, Statement startNode) {
        return findRootRecursive(testCase, startNode, 0);
    }

    private PrimitiveStatement<?> findRootRecursive(TestCase testCase, Statement stmt, int depth) {
        if (depth > 15)
            return null;
        if (stmt instanceof PrimitiveStatement)
            return (PrimitiveStatement<?>) stmt;
        if (stmt instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) stmt;
            if (!ms.getParameterReferences().isEmpty()) {
                VariableReference param = ms.getParameterReferences().get(0);
                return findRootRecursive(testCase, testCase.getStatement(param.getStPosition()), depth + 1);
            }
        }
        return null;
    }

    private void cleanupDeadStatements(TestCase testCase) {
        for (int i = testCase.size() - 1; i >= 0; i--) {
            Statement currentStatement = testCase.getStatement(i);
            if (!(currentStatement instanceof PrimitiveStatement))
                continue;
            if (!testCase.hasReferences(currentStatement.getReturnValue())) {
                if (TRACE_MUTATION_ATTEMPTS)
                    DebugStoryLogger
                            .trace("cleanupDeadStatements: Removing dead primitive: " + currentStatement.getCode());
                testCase.remove(i);
            }
        }
    }

    private String mutateNumericStringValue(String currentValue) {
        double currentDouble;
        try {
            currentDouble = Double.parseDouble(currentValue);
        } catch (NumberFormatException e) {
            return currentValue; // Should not be mutated if not a number
        }

        int mutationType = Randomness.nextInt(4);
        double newDouble = currentDouble;
        switch (mutationType) {
            case 0:
                newDouble++;
                break;
            case 1:
                newDouble--;
                break;
            case 2:
                newDouble *= -1;
                break;
            default:
                List<Double> boundaries = new ArrayList<>();
                boundaries.add(0.0);
                boundaries.add(1.0);
                boundaries.add(-1.0);
                newDouble = Randomness.choice(boundaries);
                break;
        }
        return currentValue.contains(".") ? String.valueOf(newDouble) : String.valueOf((int) newDouble);
    }

    private String mutateAlphanumericStringValue(String currentValue) {
        // NEW WEIGHTS: We will make special characters much more likely.
        // Total "tickets" in our lottery: 20
        // - Drastic (empty/whitespace): 2 tickets (10%)
        // - SPECIAL CHARACTERS: 7 tickets (35%) <-- CRANKED UP
        // - Other Major (duplicate/reverse): 3 tickets (15%)
        // - Minor (replace/delete): 8 tickets (40%)
        int mutationChoice = Randomness.nextInt(20);

        if (currentValue.isEmpty() && mutationChoice > 1) {
            // If string is empty, we must add to it. Let's force a special char or a
            // regular char.
            mutationChoice = Randomness.nextBoolean() ? 2 : 12;
        }

        if (mutationChoice <= 1) { // 10% chance
            // Drastic: Empty or whitespace
            return Randomness.nextBoolean() ? "" : "   ";
        } else if (mutationChoice <= 8) { // 35% chance
            // AGGRESSIVE: Insert a random special character
            String specialChars = "!@#$%^&*_{}|;:',.<>/?`~\"\\()=-+"; // Added more chars
            char special = specialChars.charAt(Randomness.nextInt(specialChars.length()));
            int pos = currentValue.isEmpty() ? 0 : Randomness.nextInt(currentValue.length() + 1);
            return new StringBuilder(currentValue).insert(pos, special).toString();
        } else if (mutationChoice <= 11) { // 15% chance
            // Other Major: Duplicate or reverse
            return Randomness.nextBoolean() ? (currentValue + currentValue)
                    : new StringBuilder(currentValue).reverse().toString();
        } else { // 40% chance
            // Minor: Replace or delete a character
            if (Randomness.nextBoolean() && !currentValue.isEmpty()) {
                // Replace a character
                String safeAlphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                int replacePos = Randomness.nextInt(currentValue.length());
                char randomChar = safeAlphabet.charAt(Randomness.nextInt(safeAlphabet.length()));
                StringBuilder sb = new StringBuilder(currentValue);
                sb.setCharAt(replacePos, randomChar);
                return sb.toString();
            } else if (!currentValue.isEmpty()) {
                // Delete a random character
                int deletePos = Randomness.nextInt(currentValue.length());
                return new StringBuilder(currentValue).deleteCharAt(deletePos).toString();
            } else {
                // Fallback for empty string if it gets here
                return "a";
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private String mutateEnum(EnumPrimitiveStatement primitive) {
        String varName = primitive.getReturnValue().getName();
        List<Enum> constants = new ArrayList<>(primitive.getEnumValues());
        if (constants.size() <= 1)
            return null;
        Enum currentValue = (Enum) primitive.getValue();
        constants.remove(currentValue);
        Enum newValue = Randomness.choice(constants);
        primitive.setValue(newValue);
        return String.format("Mutated ENUM '%s' from '%s' to '%s' via SWITCH_ENUM", varName, currentValue.name(),
                newValue.name());
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty())
            return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}