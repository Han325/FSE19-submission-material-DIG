package org.evosuite.enhancer.distance;

import org.evosuite.enhancer.ContextExtractor;
import org.evosuite.enhancer.VariableUsageContext;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A lightweight, fast distance calculator that compares the "semantic primitive"
 * values between two TestChromosomes. It avoids test execution and slow string parsing.
 */
public class InputVectorDistance {

    // This is only used to know the full set of semantic variables we are interested in.
    private final List<VariableUsageContext> seedSemanticContexts;

    public InputVectorDistance(List<VariableUsageContext> semanticContexts) {
        this.seedSemanticContexts = semanticContexts;
    }

    /**
     * Calculates the normalized distance between the semantic input vectors of two chromosomes.
     * @param c1 The first chromosome.
     * @param c2 The second chromosome.
     * @return A distance score between 0.0 and 1.0 per variable, summed up.
     */
    public double calculate(TestChromosome c1, TestChromosome c2) {
        if (seedSemanticContexts == null || seedSemanticContexts.isEmpty()) {
            return 0.0;
        }

        // --- NEW, LIVE LOGIC ---
        // Generate a fresh, live vector for each chromosome, every time.
        Map<String, PrimitiveStatement<?>> vector1 = getLiveSemanticPrimitiveVector(c1);
        Map<String, PrimitiveStatement<?>> vector2 = getLiveSemanticPrimitiveVector(c2);

        double totalDistance = 0.0;
        int variablesCompared = 0;

        // Compare based on the variables found in the first vector.
        for (String varName : vector1.keySet()) {
            if (vector2.containsKey(varName)) {
                PrimitiveStatement<?> p1 = vector1.get(varName);
                PrimitiveStatement<?> p2 = vector2.get(varName);

                if (p1 != null && p2 != null) {
                    totalDistance += getNormalizedDistance(p1, p2);
                    variablesCompared++;
                }
            }
        }
        
        if (variablesCompared == 0) return 0.0;
        
        // Normalize by the number of variables we successfully compared.
        return totalDistance / variablesCompared;
    }

    /**
     * Extracts a map of semantic variable names to their LIVE root primitive statements
     * by re-running the context extraction and tracing logic on the fly.
     */
    private Map<String, PrimitiveStatement<?>> getLiveSemanticPrimitiveVector(TestChromosome chromosome) {
        Map<String, PrimitiveStatement<?>> vector = new HashMap<>();
        ContextExtractor extractor = new ContextExtractor();
        List<VariableUsageContext> liveContexts = extractor.extractContexts(chromosome);

        for (VariableUsageContext liveContext : liveContexts) {
            PrimitiveStatement<?> rootPrimitive = findRootPrimitive(chromosome.getTestCase(), liveContext.getDeclarationStatement());
            if (rootPrimitive != null) {
                vector.put(liveContext.getSemanticVariable().getName(), rootPrimitive);
            }
        }
        return vector;
    }

    private PrimitiveStatement<?> findRootPrimitive(TestCase testCase, Statement startNode) {
        return findRootRecursive(testCase, startNode, 0);
    }
    
    private PrimitiveStatement<?> findRootRecursive(TestCase testCase, Statement stmt, int depth) {
        if (depth > 15) return null;
        if (stmt instanceof PrimitiveStatement) return (PrimitiveStatement<?>) stmt;

        if (stmt instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) stmt;
            if (ms.getMethodName().equals("fromString") && !ms.getParameterReferences().isEmpty()) {
                VariableReference param = ms.getParameterReferences().get(0);
                return findRootRecursive(testCase, testCase.getStatement(param.getStPosition()), depth + 1);
            }
        }
        return null;
    }

    private double getNormalizedDistance(PrimitiveStatement<?> p1, PrimitiveStatement<?> p2) {
        Object val1 = p1.getValue();
        Object val2 = p2.getValue();

        if (val1 == null || val2 == null) return val1 == val2 ? 0.0 : 1.0;
        if (val1.equals(val2)) return 0.0;

        if (val1 instanceof String) {
            int levDistance = Levenshtein.computeDistance((String) val1, (String) val2);
            return normalize((double) levDistance);
        }

        if (val1 instanceof Integer) {
            double diff = Math.abs(((Integer) val1).doubleValue() - ((Integer) val2).doubleValue());
            return normalize(diff);
        }
        
        if (val1 instanceof Double) {
            double diff = Math.abs((Double) val1 - (Double) val2);
            return normalize(diff);
        }

        if (val1 instanceof Enum) {
            return 1.0;
        }

        return 1.0;
    }

    private double normalize(double value) {
        return value / (value + 1.0);
    }
    
    private static class Levenshtein {
        public static int computeDistance(String s1, String s2) {
            if (s1 == null) s1 = "";
            if (s2 == null) s2 = "";
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();

            int[] costs = new int[s2.length() + 1];
            for (int i = 0; i <= s1.length(); i++) {
                int lastValue = i;
                for (int j = 0; j <= s2.length(); j++) {
                    if (i == 0) {
                        costs[j] = j;
                    } else {
                        if (j > 0) {
                            int newValue = costs[j - 1];
                            if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                                newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                            }
                            costs[j - 1] = lastValue;
                            lastValue = newValue;
                        }
                    }
                }
                if (i > 0) {
                    costs[s2.length()] = lastValue;
                }
            }
            return costs[s2.length()];
        }
    }
}