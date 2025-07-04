// IN FILE: Modification.java
package org.evosuite.enhancer;

/**
 * A simple data container (POJO) representing a single planned modification to a test case.
 * This decouples the decision-making from the actual modification to prevent listener-based infinite loops.
 */
public class Modification {

    // The context of the semantic variable we are changing.
    final VariableUsageContext context;

    // The new value suggested by the LLM.
    final String suggestedValue;

    public Modification(VariableUsageContext context, String suggestedValue) {
        this.context = context;
        this.suggestedValue = suggestedValue;
    }
}