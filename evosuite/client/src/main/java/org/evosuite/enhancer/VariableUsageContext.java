// IN FILE: VariableUsageContext.java
package org.evosuite.enhancer;

import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import java.util.List;

/**
 * A data container that holds the context for a high-level "semantic" variable.
 * It also tracks the usage count of its underlying root primitive to enable
 * the "Smart Duplication" strategy.
 */
public class VariableUsageContext {

    // The high-level semantic variable we are targeting (e.g., incomeDescription0).
    private final VariableReference semanticVariable;
    
    // The statement that declares this semantic variable (e.g., the call to fromString(...)).
    private final Statement declarationStatement;

    // The original, low-level primitive value that was used to create it.
    private final String originalPrimitiveValue;

    // A list of all method calls that directly use the semanticVariable.
    private final List<String> usageMethodNames;

    // --- NEW FIELD ---
    // The number of semantic variables that are derived from the same root primitive.
    private final int rootPrimitiveUsageCount;

    // --- UPDATED CONSTRUCTOR ---
    public VariableUsageContext(VariableReference semanticVariable, Statement declarationStatement,
                                String originalPrimitiveValue, List<String> usageMethodNames,
                                int rootPrimitiveUsageCount) { // New parameter added
        this.semanticVariable = semanticVariable;
        this.declarationStatement = declarationStatement;
        this.originalPrimitiveValue = originalPrimitiveValue;
        this.usageMethodNames = usageMethodNames;
        this.rootPrimitiveUsageCount = rootPrimitiveUsageCount; // Assignment for the new field
    }

    // --- GETTERS ---
    public VariableReference getSemanticVariable() { return semanticVariable; }
    public Statement getDeclarationStatement() { return declarationStatement; }
    public String getOriginalPrimitiveValue() { return originalPrimitiveValue; }
    public List<String> getUsageMethodNames() { return usageMethodNames; }
    public String getSemanticVariableType() { return semanticVariable.getVariableClass().getCanonicalName(); }
    
    // --- NEW GETTER ---
    public int getRootPrimitiveUsageCount() { return rootPrimitiveUsageCount; }

    @Override
    public String toString() {
        return "VariableUsageContext{" +
                "semanticVariable='" + semanticVariable.getName() + "' (" + getSemanticVariableType() + ")" +
                ", derivedFromValue='" + originalPrimitiveValue + '\'' +
                ", rootUsageCount=" + rootPrimitiveUsageCount + // Added for better logging
                ", usedIn=" + usageMethodNames +
                '}';
    }
}