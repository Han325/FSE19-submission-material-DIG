// IN FILE: ContextExtractor.java
package org.evosuite.enhancer;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ContextExtractor.class);
    private static final Set<String> BLACKLISTED_TYPES = new HashSet<>(Arrays.asList("custom_classes.Id", "custom_classes.Date"));

    public List<VariableUsageContext> extractContexts(TestChromosome candidate) {
        List<VariableUsageContext> contexts = new ArrayList<>();
        TestCase testCase = candidate.getTestCase();

        // A single, simple loop to find and process all targets.
        for (Statement declarationStatement : testCase) {
            VariableReference semanticVar = declarationStatement.getReturnValue();

            if (isSemanticTarget(semanticVar, declarationStatement)) {
                
                String initialValue = getInitialValueFromDeclaration(declarationStatement, testCase);

                if (initialValue != null) {
                    List<String> usages = findUsages(semanticVar, testCase);
                    
                    int usageCount = 1; 
                    
                    VariableUsageContext context = new VariableUsageContext(
                        semanticVar,
                        declarationStatement,
                        initialValue,
                        usages,
                        usageCount
                    );
                    contexts.add(context);
                }
            }
        }
        return contexts;
    }

    private boolean isSemanticTarget(VariableReference var, Statement declaration) {
        if (var == null || var.isVoid()) return false;
        String typeName = var.getVariableClass().getCanonicalName();
        if (!typeName.startsWith("custom_classes.") || BLACKLISTED_TYPES.contains(typeName)) {
            return false;
        }
        
        // The declaration must be one of these types for us to consider it.
        return declaration instanceof EnumPrimitiveStatement || 
               declaration instanceof MethodStatement;
    }

    private List<String> findUsages(VariableReference varToTrack, TestCase testCase) {
        List<String> usages = new ArrayList<>();
        for (Statement usageStatement : testCase) {
            if (usageStatement instanceof MethodStatement) {
                if (((MethodStatement) usageStatement).getParameterReferences().contains(varToTrack)) {
                    usages.add(((MethodStatement) usageStatement).getMethodName());
                }
            }
        }
        return usages.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * This is the master value-finding method. It traces back if necessary.
     */
    private String getInitialValueFromDeclaration(Statement stmt, TestCase testCase) {
        return findValueRecursive(stmt, testCase, 0);
    }
    
    private String findValueRecursive(Statement stmt, TestCase testCase, int depth) {
        if (depth > 15) { return null; }

        // Base Case: It's a primitive.
        if (stmt instanceof PrimitiveStatement) {
            Object value = ((PrimitiveStatement<?>) stmt).getValue();
            return value != null ? value.toString() : "null";
        }

        // It's a method call (e.g., fromString or getRandomInstance)
        if (stmt instanceof MethodStatement) {
            MethodStatement ms = (MethodStatement) stmt;
            String methodName = ms.getMethodName();

            // If it's fromString, we trace its parameter.
            if (methodName.equals("fromString")) {
                if (!ms.getParameterReferences().isEmpty()) {
                    VariableReference param = ms.getParameterReferences().get(0);
                    return findValueRecursive(testCase.getStatement(param.getStPosition()), testCase, depth + 1);
                }
            }
        }
        
        return null;
    }
}