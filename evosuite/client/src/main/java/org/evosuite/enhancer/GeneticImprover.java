package org.evosuite.enhancer;

import org.evosuite.enhancer.distance.InputVectorDistance;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Takes a list of LLM-enhanced seeds and generates a larger, more diverse
 * population using Genetic Improvement techniques.
 */
public class GeneticImprover {

    // --- MASTER SWITCHES ---
    private static final boolean DEEP_DIVE_MODE = false;           // SET TO true FOR DETAILED LOGGING OF ONE CANDIDATE
    private static final boolean TRACE_MUTATION_ATTEMPTS = false;  // SET TO true FOR ULTRA-DETAILED MUTATION TRACING
    private static final boolean DRY_RUN = false;                 // Master switch for GI phase

    private static final int AUDITION_POOL_SIZE = 20;    // Number of random mutations to generate per seed
    private static final int MAX_AUDITION_ATTEMPTS = 100; // Prevents infinite loops

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
                DebugStoryLogger.log("GI WARNING: Seed #" + seed.getTestCase().getID() + " has no semantic variables to mutate. Adding original seed and skipping diversification.");
                superchargedPopulation.add(seed);
                continue;
            }

            List<TestChromosome> diverseFamily = selectDiverseFamily(seed, variationsPerSeed, semanticContexts);
            superchargedPopulation.addAll(diverseFamily);
        }

        DebugStoryLogger.logGIPhaseEnd(superchargedPopulation.size());
        return superchargedPopulation;
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
        for(TestChromosome seed : seeds) {
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
        DebugStoryLogger.log("Selected seed #" + targetSeed.getTestCase().getID() + " for deep dive (has " + maxPrimitives + " semantic primitives).");
        DebugStoryLogger.log("--- DEEP DIVE: Initial Seed State ---");
        DebugStoryLogger.log(targetSeed.getTestCase().toCode());
        DebugStoryLogger.log("--- DEEP DIVE: Identified Semantic Primitives ---");
        for(VariableUsageContext ctx : targetContexts) {
            DebugStoryLogger.log("- " + ctx.getSemanticVariable().getName() + " (source: " + ctx.getDeclarationStatement().getReturnValue().getName() + ")");
        }

        List<MutationResult> auditionPool = createAuditionPool(targetSeed, targetContexts);
        DebugStoryLogger.log("\n--- DEEP DIVE: Audition Pool ("+auditionPool.size()+" candidates) ---");
        for(int i = 0; i < auditionPool.size(); i++) {
            MutationResult result = auditionPool.get(i);
            DebugStoryLogger.log(String.format("--- Auditioner #%d: %s ---", i+1, result.description));
            DebugStoryLogger.log(result.chromosome.getTestCase().toCode());
        }

        DebugStoryLogger.log("\n--- DEEP DIVE: Maximin Selection Process ---");
        List<TestChromosome> diverseFamily = new ArrayList<>();
        diverseFamily.add(targetSeed);
        InputVectorDistance distanceCalculator = new InputVectorDistance(targetContexts);

        for(int i = 1; i < 5; i++) {
            DebugStoryLogger.log(String.format("\n--- Selecting Family Member #%d ---", i+1));
            MutationResult bestResult = null;
            double maxMinDistance = -1.0;

            for (MutationResult result : auditionPool) {
                DebugStoryLogger.log("  - Evaluating Auditioner: " + result.description);
                double minDistanceToFamily = Double.MAX_VALUE;
                for (TestChromosome familyMember : diverseFamily) {
                    double dist = distanceCalculator.calculate(result.chromosome, familyMember);
                    DebugStoryLogger.log(String.format("    - Distance to family member #%d: %.4f", familyMember.getTestCase().getID(), dist));
                    if (dist < minDistanceToFamily) {
                        minDistanceToFamily = dist;
                    }
                }
                DebugStoryLogger.log(String.format("  - Min-Distance for this auditioner: %.4f", minDistanceToFamily));
                if (minDistanceToFamily > maxMinDistance) {
                    maxMinDistance = minDistanceToFamily;
                    bestResult = result;
                }
            }

            if(bestResult != null) {
                DebugStoryLogger.log(String.format("WINNER: Chosen candidate with mutation '%s'. Max-Min-Distance: %.4f. Adding to family.", bestResult.description, maxMinDistance));
                diverseFamily.add(bestResult.chromosome);
                auditionPool.remove(bestResult);
            } else {
                DebugStoryLogger.log("WINNER: Could not find any more diverse candidates.");
                break;
            }
        }

        DebugStoryLogger.log("\n--- DEEP DIVE: Final Diverse Family ---");
        for(int i = 0; i < diverseFamily.size(); i++) {
            DebugStoryLogger.log(String.format("--- Final Family Member #%d ---", i+1));
            DebugStoryLogger.log(diverseFamily.get(i).getTestCase().toCode());
        }
        DebugStoryLogger.log("==================== [DEEP DIVE COMPLETE] ====================");
    }

    private List<TestChromosome> selectDiverseFamily(TestChromosome seed, int familySize, List<VariableUsageContext> semanticContexts) {
        List<TestChromosome> diverseFamily = new ArrayList<>();
        diverseFamily.add(seed);
        InputVectorDistance distanceCalculator = new InputVectorDistance(semanticContexts);
        List<MutationResult> auditionPool = createAuditionPool(seed, semanticContexts);

        while (diverseFamily.size() < familySize && !auditionPool.isEmpty()) {
            MutationResult bestResult = null;
            double maxMinDistance = -1.0;
            for (MutationResult result : auditionPool) {
                double minDistanceToFamily = Double.MAX_VALUE;
                for (TestChromosome familyMember : diverseFamily) {
                    double dist = distanceCalculator.calculate(result.chromosome, familyMember);
                    if (dist < minDistanceToFamily) {
                        minDistanceToFamily = dist;
                    }
                }
                if (minDistanceToFamily > maxMinDistance) {
                    maxMinDistance = minDistanceToFamily;
                    bestResult = result;
                }
            }
            if (bestResult != null) {
                DebugStoryLogger.logGISelection(diverseFamily.size() + 1, familySize, bestResult.description, maxMinDistance);
                diverseFamily.add(bestResult.chromosome);
                auditionPool.remove(bestResult);
            } else {
                DebugStoryLogger.log("GI WARNING: Could not find any more diverse candidates for this seed. Family size may be smaller than requested.");
                break;
            }
        }
        return diverseFamily;
    }

    private List<MutationResult> createAuditionPool(TestChromosome seed, List<VariableUsageContext> semanticContexts) {
        List<MutationResult> pool = new ArrayList<>();
        Set<String> seenChromosomes = new HashSet<>();
        seenChromosomes.add(seed.getTestCase().toCode());
        int attempts = 0;
        while (pool.size() < AUDITION_POOL_SIZE && attempts < MAX_AUDITION_ATTEMPTS) {
            TestChromosome clone = (TestChromosome) seed.clone();
            String mutationDescription = applyRandomMutation(clone, semanticContexts, attempts + 1);
            if (mutationDescription != null) {
                if (seenChromosomes.add(clone.getTestCase().toCode())) {
                    if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("createAuditionPool: New unique mutation found. Adding to pool (Size: " + (pool.size() + 1) + ").");
                    pool.add(new MutationResult(clone, mutationDescription));
                } else {
                    if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("createAuditionPool: Duplicate mutation generated. Discarding.");
                }
            }
            attempts++;
        }
        return pool;
    }

    // --- START OF SURGICAL STRIKE ---
    private String applyRandomMutation(TestChromosome chromosome, List<VariableUsageContext> semanticContexts, int attemptNum) {
        if (semanticContexts.isEmpty()) return null;

        if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace(String.format("\n// --- Attempt %d/%d ---", attemptNum, MAX_AUDITION_ATTEMPTS));
        VariableUsageContext targetContext = Randomness.choice(semanticContexts);
        if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("applyRandomMutation: Chose semantic context for '" + targetContext.getSemanticVariable().getName() + "' (" + targetContext.getSemanticVariableType() + ").");

        Statement declarationStatement = chromosome.getTestCase().getStatement(targetContext.getDeclarationStatement().getPosition());

        // --- NEW LOGIC ---
        // Case 1: Simple Enum Mutation
        if (declarationStatement instanceof EnumPrimitiveStatement) {
            if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("applyRandomMutation: Declaration is an ENUM. Applying direct mutation...");
            return mutateEnum((EnumPrimitiveStatement) declarationStatement);
        }

        // Case 2: Smart Duplication for MethodStatements
        if (declarationStatement instanceof MethodStatement) {
            if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("applyRandomMutation: Declaration is a MethodStatement. Applying 'Smart Duplication' surgery...");
            return mutateBySmartDuplication(chromosome.getTestCase(), (MethodStatement) declarationStatement);
        }

        if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("applyRandomMutation: FAILED. Unhandled declaration type: " + declarationStatement.getClass().getSimpleName());
        return null;
    }

    private String mutateBySmartDuplication(TestCase testCase, MethodStatement methodStmt) {
        // Step 1: Find the original root primitive by tracing back
        PrimitiveStatement<?> originalPrimitive = findRootPrimitive(testCase, methodStmt);
        if (originalPrimitive == null) {
            if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("mutateBySmartDuplication: FAILED. Could not trace back to a root primitive from " + methodStmt.getCode());
            return null;
        }
        if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("mutateBySmartDuplication: Found root primitive: " + originalPrimitive.getCode());

        // Step 2: Generate a new mutated value based on the type of the original's value
        String newValue = generateMutatedValue(originalPrimitive);
        if (newValue == null) {
            if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("mutateBySmartDuplication: FAILED. Mutation resulted in no change.");
            return null;
        }
        
        // Step 3: Perform the "Smart Duplication" surgery
        try {
            int position = methodStmt.getPosition();
            StringPrimitiveStatement newPrimitiveStmt = new StringPrimitiveStatement(testCase, newValue);
            testCase.addStatement(newPrimitiveStmt, position);
            methodStmt.replaceParameterReference(newPrimitiveStmt.getReturnValue(), 0);
            
            // Step 4: Cleanup
            cleanupDeadStatements(testCase);

            // For logging purposes, let's create a description
            String originalValue = originalPrimitive.getValue().toString();
            String varName = methodStmt.getReturnValue().getName();
            return String.format("Mutated '%s' via SMART_DUPLICATION from '%s' to '%s'", varName, originalValue, newValue);
        } catch (Exception e) {
            if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("mutateBySmartDuplication: FAILED during surgery. " + e.getMessage());
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
        // Can be extended for raw numerics if needed, but current logic handles strings
        return null;
    }

    private PrimitiveStatement<?> findRootPrimitive(TestCase testCase, Statement startNode) {
        return findRootRecursive(testCase, startNode, 0);
    }
    
    private PrimitiveStatement<?> findRootRecursive(TestCase testCase, Statement stmt, int depth) {
        if (depth > 15) return null;
        if (stmt instanceof PrimitiveStatement) return (PrimitiveStatement<?>) stmt;

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
            if (!(currentStatement instanceof PrimitiveStatement)) continue;
            
            if (!testCase.hasReferences(currentStatement.getReturnValue())) {
                if (TRACE_MUTATION_ATTEMPTS) DebugStoryLogger.trace("cleanupDeadStatements: Removing dead primitive: " + currentStatement.getCode());
                testCase.remove(i);
            }
        }
    }

    // --- VALUE GENERATION HELPERS (These now return values, not modify statements) ---
    private String mutateNumericStringValue(String currentValue) {
        double currentDouble = Double.parseDouble(currentValue);
        int mutationType = Randomness.nextInt(4);
        double newDouble = currentDouble;
        switch (mutationType) {
            case 0: newDouble++; break;
            case 1: newDouble--; break;
            case 2: newDouble *= -1; break;
            default:
                List<Double> boundaries = new ArrayList<>();
                boundaries.add(0.0); boundaries.add(1.0); boundaries.add(-1.0);
                newDouble = Randomness.choice(boundaries);
                break;
        }
        return currentValue.contains(".") ? String.valueOf(newDouble) : String.valueOf((int)newDouble);
    }

    private String mutateAlphanumericStringValue(String currentValue) {
        int mutationType = Randomness.nextInt(3);
        switch (mutationType) {
            case 0: return "";
            case 1:
                if (!currentValue.isEmpty()) {
                    int pos = Randomness.nextInt(currentValue.length());
                    char randomChar = (char) (Randomness.nextInt(95) + 32);
                    StringBuilder sb = new StringBuilder(currentValue);
                    sb.setCharAt(pos, randomChar);
                    return sb.toString();
                }
                return currentValue;
            default:
                String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>/?`~";
                char special = specialChars.charAt(Randomness.nextInt(specialChars.length()));
                int pos = currentValue.isEmpty() ? 0 : Randomness.nextInt(currentValue.length() + 1);
                StringBuilder sb = new StringBuilder(currentValue);
                sb.insert(pos, special);
                return sb.toString();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String mutateEnum(EnumPrimitiveStatement primitive) {
        String varName = primitive.getReturnValue().getName();
        List<Enum> constants = new ArrayList<>(primitive.getEnumValues());
        if (constants.size() <= 1) return null;
        Enum currentValue = (Enum) primitive.getValue();
        constants.remove(currentValue);
        Enum newValue = Randomness.choice(constants);
        primitive.setValue(newValue);
        return String.format("Mutated ENUM '%s' from '%s' to '%s' via SWITCH_ENUM", varName, currentValue.name(), newValue.name());
    }

    // --- OLD HELPERS (kept for reference, but no longer used by the main path) ---
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}