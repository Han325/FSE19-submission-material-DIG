package org.evosuite.enhancer;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
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
                GenericMethod genericMethod = methodStatement.getMethod();
                if (genericMethod.getMethod().getParameterCount() > 0) {
                    contexts.addAll(processMethodStatement(methodStatement));
                }
            }
        }
        return contexts;
    }

    /**
     * Processes a single MethodStatement to get context for all its parameters.
     */
    private List<ParameterContext> processMethodStatement(MethodStatement methodStatement) {
        List<ParameterContext> methodContexts = new ArrayList<>();
        GenericMethod genericMethod = methodStatement.getMethod();
        Method javaMethod = genericMethod.getMethod();

        Parameter[] parameters = javaMethod.getParameters();

        // --- THIS IS THE CORRECTED LOGIC ---
        // We use getParameterReferences() which returns an ORDERED LIST of ONLY the parameters.
        // This is much cleaner and safer than using the unordered Set from getVariableReferences().
        List<VariableReference> paramVarRefs = methodStatement.getParameterReferences();

        // Now, the `parameters` array and `paramVarRefs` list are parallel and have the same size.
        // We can safely iterate over them together.
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            VariableReference paramVarRef = paramVarRefs.get(i); // Safe indexed access

            String methodName = javaMethod.getName();
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            String initialValueString = getVariableValueAsString(paramVarRef, param.getDeclaringExecutable().getDeclaringClass().getClassLoader());

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
     * This is the most complex part of the extraction and requires runtime debugging.
     */
    private String getVariableValueAsString(VariableReference varRef, ClassLoader cl) {
        // TODO: This is a placeholder for the actual implementation.
        // The real logic needs to inspect the varRef, find its declaration statement,
        // and extract the value from that statement (e.g., from a PrimitiveStatement).
        // This is complex and MUST be developed while debugging at a breakpoint.
        // For now, we return a clear placeholder.
        try {
            if (varRef.isPrimitive()) {
                // This is still a conceptual placeholder for the logic that needs to be
                // implemented during a debug session.
            }
        } catch (Exception e) {
            // Fallback
        }
        return "VALUE_EXTRACTION_TODO: " + varRef.getName();
    }
}