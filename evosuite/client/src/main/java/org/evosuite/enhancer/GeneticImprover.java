package org.evosuite.enhancer;

import org.evosuite.enhancer.distance.InputVectorDistance;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.EnumPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
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
    private static final boolean DEEP_DIVE_MODE = true; // SET TO true FOR DETAILED LOGGING OF ONE CANDIDATE
    private static final boolean DRY_RUN = false;      // Master switch for GI phase

    private static final int AUDITION_POOL_SIZE = 20;    // Number of random mutations to generate per seed
    private static final int MAX_AUDITION_ATTEMPTS = 100; // Prevents infinite loops

    /**
     * A simple inner class to pair a mutated chromosome with the description of how it was mutated.
     */
    private static class MutationResult {
        final TestChromosome chromosome;
        final String description;

        MutationResult(TestChromosome chromosome, String description) {
            this.chromosome = chromosome;
            this.description = description;
        }
    }

    public GeneticImprover() {
        // Constructor for the GI module
    }

    /**
     * The main entry point for Phase B.
     * @param seeds The list of k LLM-blessed candidates.
     * @param variationsPerSeed The number of m diverse variations to create for each seed.
     * @return A new, larger list of k * m "supercharged" candidates.
     */
    public List<TestChromosome> diversifyPopulation(List<TestChromosome> seeds, int variationsPerSeed) {
        if (DEEP_DIVE_MODE) {
            runDeepDive(seeds);
            // In deep dive mode, we don't generate a population, we just log.
            // Return the original seeds so the process can continue without error.
            return seeds;
        }
        
        if (DRY_RUN) {
            DebugStoryLogger.logGIPhaseStart(seeds.size(), variationsPerSeed, true);
            return seeds; // Return original seeds if dry run
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

    /**
     * A special debugging mode that produces extremely detailed logs for a single candidate.
     * @param seeds The full list of seeds to select from.
     */
    private void runDeepDive(List<TestChromosome> seeds) {
        if (seeds.isEmpty()) {
            DebugStoryLogger.log("DEEP DIVE: Seed list is empty. Aborting.");
            return;
        }

        // Find the seed with the most potential mutation targets
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

        for(int i = 1; i < 5; i++) { // Generate a family of 5
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
            String mutationDescription = applyRandomMutation(clone, semanticContexts);

            if (mutationDescription != null && seenChromosomes.add(clone.getTestCase().toCode())) {
                pool.add(new MutationResult(clone, mutationDescription));
            }
            attempts++;
        }
        return pool;
    }

    // --- START OF SURGICAL STRIKE ---
    // THIS IS THE NEW, FIXED-UP MUTATION LOGIC
    private String applyRandomMutation(TestChromosome chromosome, List<VariableUsageContext> semanticContexts) {
        if (semanticContexts.isEmpty()) return null;

        VariableUsageContext targetContext = Randomness.choice(semanticContexts);
        Statement stmt = chromosome.getTestCase().getStatement(targetContext.getDeclarationStatement().getPosition());

        if (!(stmt instanceof PrimitiveStatement)) {
            return null;
        }
        PrimitiveStatement<?> primitive = (PrimitiveStatement<?>) stmt;

        // "Parse & Detect" Gauntlet
        if (primitive instanceof EnumPrimitiveStatement) {
            return mutateEnum(primitive);
        } else if (primitive instanceof IntPrimitiveStatement || primitive instanceof DoublePrimitiveStatement) {
            return mutateRawNumeric(primitive);
        } else if (primitive instanceof StringPrimitiveStatement) {
            String value = (String) primitive.getValue();
            if (isNumeric(value)) {
                return mutateNumericString(primitive);
            } else {
                return mutateAlphanumericString(primitive);
            }
        }
        return null;
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // --- NEW, SMARTER MUTATION OPERATORS ---

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String mutateRawNumeric(PrimitiveStatement primitive) {
        // This handles raw int, double, etc.
        Number currentValue = (Number) primitive.getValue();
        String varName = primitive.getReturnValue().getName();
        int mutationType = Randomness.nextInt(4);
        Number newValue = currentValue;
        String operatorName;

        // For simplicity, we'll work with doubles and cast back
        double currentDouble = currentValue.doubleValue();
        double newDouble = currentDouble;

        switch (mutationType) {
            case 0:
                operatorName = "INCREMENT";
                newDouble = currentDouble + 1.0;
                break;
            case 1:
                operatorName = "DECREMENT";
                newDouble = currentDouble - 1.0;
                break;
            case 2:
                operatorName = "NEGATE";
                newDouble = currentDouble * -1.0;
                break;
            default:
                operatorName = "SET_TO_BOUNDARY";
                List<Double> boundaries = new ArrayList<>();
                boundaries.add(0.0);
                boundaries.add(1.0);
                boundaries.add(-1.0);
                newValue = Randomness.choice(boundaries);
                newDouble = (Double) newValue;
                break;
        }

        // Cast back to original type
        if (primitive.getValue() instanceof Integer) {
            newValue = (int) newDouble;
        } else {
            newValue = newDouble;
        }
        
        if (newValue.equals(currentValue)) return null;
        primitive.setValue(newValue);
        return String.format("Mutated RAW NUMERIC '%s' from '%s' to '%s' via %s", varName, currentValue, newValue, operatorName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String mutateNumericString(PrimitiveStatement primitive) {
        String currentValue = (String) primitive.getValue();
        String varName = primitive.getReturnValue().getName();
        double currentDouble;
        try {
             currentDouble = Double.parseDouble(currentValue);
        } catch (NumberFormatException e) {
            return null; // Should not happen due to isNumeric check
        }

        int mutationType = Randomness.nextInt(4);
        double newDouble = currentDouble;
        String operatorName;

        switch (mutationType) {
            case 0:
                operatorName = "INCREMENT";
                newDouble = currentDouble + 1.0;
                break;
            case 1:
                operatorName = "DECREMENT";
                newDouble = currentDouble - 1.0;
                break;
            case 2:
                operatorName = "NEGATE";
                newDouble = currentDouble * -1.0;
                break;
            default:
                operatorName = "SET_TO_BOUNDARY";
                List<Double> boundaries = new ArrayList<>();
                boundaries.add(0.0);
                boundaries.add(1.0);
                boundaries.add(-1.0);
                newDouble = Randomness.choice(boundaries);
                break;
        }

        // Keep original format (int or double)
        String newValue = currentValue.contains(".") ? String.valueOf(newDouble) : String.valueOf((int)newDouble);
        
        if (newValue.equals(currentValue)) return null;
        primitive.setValue(newValue);
        return String.format("Mutated NUMERIC_STRING '%s' from '%s' to '%s' via %s", varName, currentValue, newValue, operatorName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String mutateAlphanumericString(PrimitiveStatement primitive) {
        String currentValue = (String) primitive.getValue();
        String varName = primitive.getReturnValue().getName();
        int mutationType = Randomness.nextInt(3);
        String newValue = currentValue;
        String operatorName;

        switch (mutationType) {
            case 0:
                operatorName = "SET_TO_EMPTY";
                newValue = "";
                break;
            case 1:
                operatorName = "CHAR_FLIP";
                if (!currentValue.isEmpty()) {
                    int pos = Randomness.nextInt(currentValue.length());
                    char randomChar = (char) (Randomness.nextInt(95) + 32); // Printable ASCII
                    StringBuilder sb = new StringBuilder(currentValue);
                    sb.setCharAt(pos, randomChar);
                    newValue = sb.toString();
                }
                break;
            default:
                operatorName = "INSERT_SPECIAL_CHAR";
                String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>/?`~";
                char special = specialChars.charAt(Randomness.nextInt(specialChars.length()));
                int pos = currentValue.isEmpty() ? 0 : Randomness.nextInt(currentValue.length() + 1);
                StringBuilder sb = new StringBuilder(currentValue);
                sb.insert(pos, special);
                newValue = sb.toString();
                break;
        }
        if (newValue.equals(currentValue)) return null;
        primitive.setValue(newValue);
        return String.format("Mutated ALPHANUMERIC_STRING '%s' from '%s' to '%s' via %s", varName, currentValue, newValue, operatorName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String mutateEnum(PrimitiveStatement primitive) {
        String varName = primitive.getReturnValue().getName();
        EnumPrimitiveStatement enumStmt = (EnumPrimitiveStatement) primitive;
        List<Enum> constants = new ArrayList<>(enumStmt.getEnumValues());
        if (constants.size() <= 1) return null;

        Enum currentValue = (Enum) primitive.getValue();
        constants.remove(currentValue);
        
        Enum newValue = Randomness.choice(constants);
        primitive.setValue(newValue);
        return String.format("Mutated ENUM '%s' from '%s' to '%s' via SWITCH_ENUM", varName, currentValue.name(), newValue.name());
    }
}