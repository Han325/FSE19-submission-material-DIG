package org.evosuite.enhancer.distance;

import org.evosuite.testcase.statements.EnumPrimitiveStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;

import java.util.Map;

/**
 * A lightweight, fast distance calculator that compares the "semantic
 * primitive"
 * values between two TestChromosomes. It now uses pre-computed vectors and a
 * weighting system to ensure both performance and selection accuracy.
 */
public class InputVectorDistance {

    // --- NEW WEIGHTS TO FIX THE BIAS ---
    private static final double WEIGHT_STRING_NUMERIC = 2.0; // Prioritize these more complex mutations
    private static final double WEIGHT_ENUM = 1.0; // De-prioritize simple enum flips


    public InputVectorDistance() {
        // Constructor is now empty
    }

    /**
     * Calculates the weighted, normalized distance between two pre-computed input
     * vectors.
     * 
     * @param vector1 The pre-computed vector for the first chromosome.
     * @param vector2 The pre-computed vector for the second chromosome.
     * @return A weighted distance score.
     */
    public double calculate(Map<String, PrimitiveStatement<?>> vector1, Map<String, PrimitiveStatement<?>> vector2) {
        if (vector1 == null || vector1.isEmpty() || vector2 == null || vector2.isEmpty()) {
            return 0.0;
        }

        double totalNormalizedDistance = 0.0;

        for (String varName : vector1.keySet()) {
            if (vector2.containsKey(varName)) {
                PrimitiveStatement<?> p1 = vector1.get(varName);
                PrimitiveStatement<?> p2 = vector2.get(varName);

                if (p1 != null && p2 != null) {
                    // 1. Get the raw distance for this one variable.
                    double rawDistance = getDistance(p1, p2);
                    // 2. Normalize it to a 0-1 score and add it to the total.
                    totalNormalizedDistance += normalize(rawDistance);
                }
            }
        }
        
        // Return the sum of normalized scores. No cap needed.
        return totalNormalizedDistance;
    }

    private double normalize(double value) {
        // This squashes any value into a 0-1 range, where larger values get closer to 1.
        // It properly represents "more different" without creating runaway scores.
        return value / (value + 1.0);
    }

    private double getDistance(PrimitiveStatement<?> p1, PrimitiveStatement<?> p2) {
        Object val1 = p1.getValue();
        Object val2 = p2.getValue();

        if (val1 == null || val2 == null)
            return (val1 == val2 ? 0.0 : 1.0) * WEIGHT_STRING_NUMERIC;
        if (val1.equals(val2))
            return 0.0;

        double rawDistance = 0.0;

        // --- APPLY WEIGHTS BASED ON PRIMITIVE TYPE ---
        if (p1 instanceof EnumPrimitiveStatement) {
            rawDistance = 1.0 * WEIGHT_ENUM;
        } else if (val1 instanceof String) {
            int levDistance = Levenshtein.computeDistance((String) val1, (String) val2);
            rawDistance = (double) levDistance * WEIGHT_STRING_NUMERIC;
        } else if (val1 instanceof Integer) {
            double diff = Math.abs(((Integer) val1).doubleValue() - ((Integer) val2).doubleValue());
            rawDistance = diff * WEIGHT_STRING_NUMERIC;
        } else if (val1 instanceof Double) {
            double diff = Math.abs((Double) val1 - (Double) val2);
            rawDistance = diff * WEIGHT_STRING_NUMERIC;
        } else {
            rawDistance = 1.0 * WEIGHT_STRING_NUMERIC;
        }

        // Apply the safety cap to prevent any single mutation from overpowering the total score.
        return rawDistance;
    }

    private static class Levenshtein {
        public static int computeDistance(String s1, String s2) {
            if (s1 == null)
                s1 = "";
            if (s2 == null)
                s2 = "";
            s1 = s1.toLowerCase();
            s2 = s2.toLowerCase();
            int[] costs = new int[s2.length() + 1];
            for (int i = 0; i <= s1.length(); i++) {
                int lastValue = i;
                for (int j = 0; j <= s2.length(); j++) {
                    if (i == 0)
                        costs[j] = j;
                    else if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
                if (i > 0)
                    costs[s2.length()] = lastValue;
            }
            return costs[s2.length()];
        }
    }
}