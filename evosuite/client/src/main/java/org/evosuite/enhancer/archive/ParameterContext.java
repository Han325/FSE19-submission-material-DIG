package org.evosuite.enhancer.archive;

import org.evosuite.testcase.statements.MethodStatement;
import java.util.List;

/**
 * A simple Plain Old Java Object (POJO) to hold all the extracted context
 * for a single parameter that we intend to enhance using an LLM.
 */
public class ParameterContext {

    private final String methodName;
    private final String parameterName;
    private final String parameterJavaType;
    private final String initialValue;
    private final List<String> possibleEnumValues; // Null if not an enum

    // Keep a reference to the original statement and the parameter's index
    // so we can modify it later after getting the LLM's suggestion.
    private final MethodStatement originalStatement;
    private final int parameterIndex;


    public ParameterContext(String methodName, String parameterName, String parameterJavaType,
                            String initialValue, List<String> possibleEnumValues,
                            MethodStatement originalStatement, int parameterIndex) {
        this.methodName = methodName;
        this.parameterName = parameterName;
        this.parameterJavaType = parameterJavaType;
        this.initialValue = initialValue;
        this.possibleEnumValues = possibleEnumValues;
        this.originalStatement = originalStatement;
        this.parameterIndex = parameterIndex;
    }

    // --- Getters for all fields ---
    public String getMethodName() { return methodName; }
    public String getParameterName() { return parameterName; }
    public String getParameterJavaType() { return parameterJavaType; }
    public String getInitialValue() { return initialValue; }
    public List<String> getPossibleEnumValues() { return possibleEnumValues; }
    public MethodStatement getOriginalStatement() { return originalStatement; }
    public int getParameterIndex() { return parameterIndex; }


    @Override
    public String toString() {
        return "ParameterContext{" +
                "method='" + methodName + '\'' +
                ", param='" + parameterName + '\'' +
                ", type='" + parameterJavaType + '\'' +
                ", initialValue='" + initialValue + '\'' +
                ", enums=" + (possibleEnumValues != null ? possibleEnumValues.size() : "N/A") +
                '}';
    }
}