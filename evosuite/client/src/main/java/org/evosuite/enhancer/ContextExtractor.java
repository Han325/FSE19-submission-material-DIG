package org.evosuite.enhancer;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts structured context for each parameter in a TestChromosome.
 * This uses the verified EvoSuite API to inspect test case objects.
 */
public class ContextExtractor {

    /**
     * Extracts a list of all enhanceable parameters from a single TestChromosome.
     */
    public List<ParameterContext> extractContexts(TestChromosome candidate) {
        List<ParameterContext> contexts = new ArrayList<>();
        TestCase testCase = candidate.getTestCase();

        for (int i = 0; i < testCase.size(); i++) {
            Statement statement = testCase.getStatement(i);

            if (statement instanceof MethodStatement) {
                MethodStatement methodStatement = (MethodStatement) statement;

                if (methodStatement.getMethod().getMethod().getParameterCount() > 0) {
                    contexts.addAll(processMethodStatement(methodStatement, testCase));
                }
            }
        }
        return contexts;
    }

    /**
     * Processes a single MethodStatement to get context for all its parameters.
     */
    private List<ParameterContext> processMethodStatement(MethodStatement methodStatement, TestCase testCase) {
        List<ParameterContext> methodContexts = new ArrayList<>();
        GenericMethod genericMethod = methodStatement.getMethod();
        Method javaMethod = genericMethod.getMethod();

        Parameter[] parameters = javaMethod.getParameters();
        List<VariableReference> paramVarRefs = methodStatement.getParameterReferences();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            VariableReference paramVarRef = paramVarRefs.get(i);

            String methodName = javaMethod.getName();
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            
            String initialValueString = getVariableValueAsString(paramVarRef, testCase, param.getDeclaringExecutable().getDeclaringClass().getClassLoader());

            List<String> enumValues = null;
            if (paramType.isEnum()) {
                enumValues = Arrays.stream(paramType.getEnumConstants())
                                     .map(Object::toString)
                                     .collect(Collectors.toList());
            }

            ParameterContext context = new ParameterContext(
                    methodName, paramName, paramType.getName(), initialValueString,
                    enumValues, methodStatement, i
            );
            methodContexts.add(context);
        }
        return methodContexts;
    }

    /**
     * A helper function to get the string representation of a parameter's initial value.
     */
    private String getVariableValueAsString(VariableReference varRef, TestCase testCase, ClassLoader cl) {
        return extractValueRecursive(varRef, testCase, 0);
    }

    /**
     * Recursively extracts the value of a variable reference.
     */
    private String extractValueRecursive(VariableReference varRef, TestCase testCase, int depth) {
        if (depth > 10) {
            return "RECURSION_DEPTH_EXCEEDED";
        }

        Statement declarationStatement = testCase.getStatement(varRef.getStPosition());

        if (declarationStatement instanceof PrimitiveStatement) {
            PrimitiveStatement<?> primStmt = (PrimitiveStatement<?>) declarationStatement;
            Object value = primStmt.getValue();
            if (value == null) {
                return "null";
            }
            return value.toString();
        }

        if (declarationStatement instanceof ConstructorStatement) {
            ConstructorStatement consStmt = (ConstructorStatement) declarationStatement;

            if (!consStmt.getParameterReferences().isEmpty()) {
                VariableReference constructorParamRef = consStmt.getParameterReferences().get(0);
                return extractValueRecursive(constructorParamRef, testCase, depth + 1);
            } else {
                return "new " + consStmt.getDeclaringClassName() + "()";
            }
        }

        return "UNHANDLED_TYPE:" + declarationStatement.getClass().getSimpleName();
    }
}