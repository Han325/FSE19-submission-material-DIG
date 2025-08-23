package org.evosuite.enhancer;

import org.evosuite.enhancer.distance.InputVectorDistance;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Takes a list of LLM-enhanced seeds and generates a larger, more diverse
 * population using Genetic Improvement techniques. This version uses a
 * "Plausible Diversity" model, aiming to create variations that are significant
 * yet logically sound, to explore different valid application states.
 */
public class GeneticImprover {

    // --- MASTER SWITCHES ---
    private static final boolean DEEP_DIVE_MODE = false; // SET TO false FOR PRODUCTION RUNS
    private static final boolean TRACE_MUTATION_ATTEMPTS = false; // SET TO false FOR PRODUCTION RUNS
    private static final boolean DRY_RUN = false; // Master switch for GI phase

    // --- TUNABLE PARAMETERS ---
    private static final int AUDITION_POOL_SIZE = 50;
    private static final int MAX_AUDITION_ATTEMPTS = 100;
    private static final int MAX_MUTATIONS_PER_CANDIDATE = 3;

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

    // This method is for debugging and is disabled in production runs.
    private void runDeepDive(List<TestChromosome> seeds) {
        // ... (The runDeepDive method remains the same as your version)
    }

    private List<TestChromosome> selectDiverseFamily(TestChromosome seed, int familySize,
            List<VariableUsageContext> semanticContexts) {
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
        return diverseFamily;
    }

    private List<MutationResult> createAuditionPool(TestChromosome seed, List<VariableUsageContext> initialContexts) {
        List<MutationResult> pool = new ArrayList<>();
        Set<String> seenChromosomes = new HashSet<>();
        seenChromosomes.add(seed.getTestCase().toCode());

        // --- THE "SELF-SOURCING" DYNAMIC DICTIONARY ---
        Set<String> plausibleWords = new HashSet<>();
        for (Statement stmt : seed.getTestCase()) {
            if (stmt instanceof StringPrimitiveStatement) {
                String value = ((StringPrimitiveStatement) stmt).getValue();
                if (value != null) {
                    for (String word : value.split("\\s+")) {
                        if (word.length() > 1) {
                            plausibleWords.add(word);
                        }
                    }
                }
            }
        }
        List<String> plausibleWordsList = new ArrayList<>(plausibleWords);
        // --- END OF DICTIONARY LOGIC ---

        // --- PRE-FILTERING FOR EFFICIENCY ---
        List<VariableUsageContext> trulyMutableContexts = new ArrayList<>();
        for (VariableUsageContext context : initialContexts) {
            Statement declaration = seed.getTestCase().getStatement(context.getDeclarationStatement().getPosition());
            if (declaration instanceof MethodStatement) {
                trulyMutableContexts.add(context);
            }
        }

        int attempts = 0;
        while (pool.size() < AUDITION_POOL_SIZE && attempts < MAX_AUDITION_ATTEMPTS) {
            attempts++;
            TestChromosome clone = (TestChromosome) seed.clone();

            List<VariableUsageContext> availableContexts = new ArrayList<>(trulyMutableContexts);
            Randomness.shuffle(availableContexts);

            int mutationBudget = Randomness.nextInt(MAX_MUTATIONS_PER_CANDIDATE) + 1;
            List<String> mutationDescriptions = new ArrayList<>();
            Set<String> mutatedVariableNames = new HashSet<>();

            for (VariableUsageContext contextToMutate : availableContexts) {
                if (mutatedVariableNames.size() >= mutationBudget)
                    break;

                String varName = contextToMutate.getSemanticVariable().getName();
                if (mutatedVariableNames.contains(varName))
                    continue;

                String desc = applyRandomMutation(clone, Collections.singletonList(contextToMutate), plausibleWordsList,
                        attempts);

                if (desc != null) {
                    mutationDescriptions.add(desc);
                    mutatedVariableNames.add(varName);
                }
            }

            if (!mutationDescriptions.isEmpty()) {
                if (seenChromosomes.add(clone.getTestCase().toCode())) {
                    String finalDescription = String.format("%d mutations applied: %s",
                            mutationDescriptions.size(),
                            String.join(" | ", mutationDescriptions));
                    pool.add(new MutationResult(clone, finalDescription));
                }
            }
        }
        return pool;
    }

    private String applyRandomMutation(TestChromosome chromosome, List<VariableUsageContext> semanticContexts,
            List<String> dictionary, int attemptNum) {
        if (semanticContexts.isEmpty())
            return null;

        VariableUsageContext targetContext = Randomness.choice(semanticContexts);
        Statement declarationStatement = chromosome.getTestCase()
                .getStatement(targetContext.getDeclarationStatement().getPosition());

        if (declarationStatement instanceof MethodStatement) {
            return mutateBySmartDuplication(chromosome.getTestCase(), (MethodStatement) declarationStatement,
                    dictionary);
        }

        return null; // Should not be reached due to pre-filtering
    }

    private String mutateBySmartDuplication(TestCase testCase, MethodStatement methodStmt, List<String> dictionary) {
        PrimitiveStatement<?> originalPrimitive = findRootPrimitive(testCase, methodStmt);
        if (originalPrimitive == null)
            return null;

        String newValue = generateMutatedValue(originalPrimitive, dictionary);
        if (newValue == null || newValue.equals(originalPrimitive.getValue().toString()))
            return null;

        try {
            int position = methodStmt.getPosition();
            StringPrimitiveStatement newPrimitiveStmt = new StringPrimitiveStatement(testCase, newValue);
            testCase.addStatement(newPrimitiveStmt, position);
            methodStmt.replaceParameterReference(newPrimitiveStmt.getReturnValue(), 0);
            cleanupDeadStatements(testCase);
            String originalValue = originalPrimitive.getValue().toString();
            String varName = methodStmt.getReturnValue().getName();
            return String.format("Mutated '%s' via PLAUSIBLE_GI from '%s' to '%s'", varName, originalValue, newValue);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateMutatedValue(PrimitiveStatement<?> primitive, List<String> dictionary) {
        if (primitive instanceof StringPrimitiveStatement) {
            String value = (String) primitive.getValue();
            if (isNumeric(value)) {
                return mutateNumericStringValue(value);
            } else {
                return mutateAlphanumericStringValue(value, dictionary);
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
                testCase.remove(i);
            }
        }
    }

    // --- "PLAUSIBLE DIVERSITY" MUTATION OPERATORS ---

    private String mutateNumericStringValue(String currentValue) {
        double currentDouble;
        try {
            currentDouble = Double.parseDouble(currentValue);
        } catch (NumberFormatException e) {
            return shuffleString(currentValue); // If not a valid number, just shuffle it.
        }

        int mutationType = Randomness.nextInt(5);

        switch (mutationType) {
            case 0: // Small integer delta
                return String.format("%.2f", currentDouble + (Randomness.nextInt(5) + 1));
            case 1: // Small percentage change
                return String.format("%.2f", currentDouble * (1.0 + (Randomness.nextDouble() * 0.2 - 0.1)));
            case 2: // Common Value Swap
                double[] commonPrices = { 0.99, 10.00, 19.95, 49.50, 99.99, 150.00, 999.00 };
                return String.format("%.2f", commonPrices[Randomness.nextInt(commonPrices.length)]);
            case 3: // Re-order digits
                return shuffleString(currentValue);
            default: // Small negative delta
                double result = currentDouble - (Randomness.nextInt(5) + 1);
                return String.format("%.2f", Math.max(0, result)); // Prevent going excessively negative
        }
    }

    private String mutateAlphanumericStringValue(String currentValue, List<String> dictionary) {
        if (currentValue.isEmpty()) {
            return dictionary.isEmpty() ? "a" : Randomness.choice(dictionary);
        }

        int mutationType = Randomness.nextInt(10);

        switch (mutationType) {
            case 0:
            case 1:
            case 2: // 30% chance: Plausible Word Swap
                String swapped = plausibleWordSwap(currentValue, dictionary);
                return swapped.equals(currentValue) ? createPlausibleLongString(currentValue) : swapped;

            case 3:
            case 4: // 20% chance: Plausible Long String
                return createPlausibleLongString(currentValue);

            case 5:
            case 6: // 20% chance: Plausible Typo
                return createTypo(currentValue);

            default: // 30% chance: Other structural changes
                if (Randomness.nextBoolean()) { // Add/Remove Suffix
                    if (currentValue.length() > 4 && Randomness.nextBoolean()) {
                        return currentValue.substring(0, currentValue.length() - (Randomness.nextInt(3) + 1));
                    } else {
                        String[] suffixes = { "s", "er", "ing", "y", "ie" };
                        return currentValue + Randomness.choice(suffixes);
                    }
                } else { // Swap adjacent characters
                    if (currentValue.length() < 2)
                        return currentValue;
                    int pos = Randomness.nextInt(currentValue.length() - 1);
                    char[] chars = currentValue.toCharArray();
                    char temp = chars[pos];
                    chars[pos] = chars[pos + 1];
                    chars[pos + 1] = temp;
                    return new String(chars);
                }
        }
    }

    // --- HELPER METHODS FOR "PLAUSIBLE DIVERSITY" ---

    private String plausibleWordSwap(String text, List<String> dictionary) {
        if (dictionary.isEmpty())
            return text;

        String[] words = text.split("\\s+");
        if (words.length == 0)
            return text;

        int wordIndex = Randomness.nextInt(words.length);
        String originalWord = words[wordIndex];
        String newWord = Randomness.choice(dictionary);

        // Ensure we actually change the word
        int swapAttempts = 0;
        while (newWord.equalsIgnoreCase(originalWord) && dictionary.size() > 1 && swapAttempts < 5) {
            newWord = Randomness.choice(dictionary);
            swapAttempts++;
        }

        words[wordIndex] = newWord;
        return String.join(" ", words);
    }

    private String createPlausibleLongString(String text) {
        String[] words = text.split("\\s+");
        if (words.length == 0)
            return text + " " + text + " " + text;
        String wordToRepeat = words[Randomness.nextInt(words.length)];
        if (wordToRepeat.length() < 2)
            return text;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(wordToRepeat).append(" ");
        }
        return sb.toString().trim();
    }

    private String shuffleString(String text) {
        if (text == null || text.length() <= 1)
            return text;
        List<Character> characters = new ArrayList<>();
        for (char c : text.toCharArray()) {
            characters.add(c);
        }
        for (int i = characters.size() - 1; i > 0; i--) {
            // Pick a random index from 0 to i (inclusive).
            // We KNOW Randomness.nextInt() exists and works.
            int indexToSwap = Randomness.nextInt(i + 1);

            // Swap the elements.
            char temp = characters.get(i);
            characters.set(i, characters.get(indexToSwap));
            characters.set(indexToSwap, temp);
        }
        StringBuilder sb = new StringBuilder();
        for (char c : characters) {
            sb.append(c);
        }
        return sb.toString();
    }

    private String createTypo(String text) {
        if (text.isEmpty())
            return "a";
        Map<Character, String> keyboardNeighbors = new HashMap<>();
        keyboardNeighbors.put('q', "wa");
        keyboardNeighbors.put('w', "qase");
        keyboardNeighbors.put('e', "wsdr");
        keyboardNeighbors.put('r', "edft");
        keyboardNeighbors.put('t', "rfgy");
        keyboardNeighbors.put('y', "tghu");
        keyboardNeighbors.put('a', "qwsz");
        keyboardNeighbors.put('s', "awedxz");
        keyboardNeighbors.put('d', "serfcx");
        // (can be expanded)

        int pos = Randomness.nextInt(text.length());
        char originalChar = Character.toLowerCase(text.charAt(pos));

        if (keyboardNeighbors.containsKey(originalChar)) {
            String neighbors = keyboardNeighbors.get(originalChar);
            char typoChar = neighbors.charAt(Randomness.nextInt(neighbors.length()));
            StringBuilder sb = new StringBuilder(text);
            sb.setCharAt(pos, typoChar);
            return sb.toString();
        }

        return text + "s"; // Fallback
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