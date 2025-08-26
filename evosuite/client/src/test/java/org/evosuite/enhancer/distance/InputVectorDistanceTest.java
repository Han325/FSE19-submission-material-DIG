package org.evosuite.enhancer.distance;

import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.DoublePrimitiveStatement;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class InputVectorDistanceTest {

    private InputVectorDistance distance;
    private TestCase testCase;

    @Before
    public void setUp() {
        distance = new InputVectorDistance();
        testCase = new DefaultTestCase();
    }

    @Test
    public void testCalculateWithEmptyVectors() {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        double result = distance.calculate(vector1, vector2);
        assertEquals("Empty vectors should have zero distance", 0.0, result, 0.001);
    }

    @Test
    public void testCalculateWithNullVectors() {
        double result1 = distance.calculate(null, null);
        assertEquals("Null vectors should have zero distance", 0.0, result1, 0.001);
        
        Map<String, PrimitiveStatement<?>> vector = new HashMap<>();
        double result2 = distance.calculate(vector, null);
        assertEquals("Null vector should have zero distance", 0.0, result2, 0.001);
        
        double result3 = distance.calculate(null, vector);
        assertEquals("Null vector should have zero distance", 0.0, result3, 0.001);
    }

    @Test
    public void testCalculateWithIdenticalStringVectors() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "test");
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "test");
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result = distance.calculate(vector1, vector2);
        assertEquals("Identical strings should have zero distance", 0.0, result, 0.001);
    }

    @Test
    public void testCalculateWithDifferentStrings() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "hello");
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "world");
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result = distance.calculate(vector1, vector2);
        assertTrue("Different strings should have positive distance", result > 0.0);
    }

    @Test
    public void testCalculateWithLevenshteinDistance() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        // Test strings with known Levenshtein distance
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "cat");
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "bat"); // distance = 1
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result1 = distance.calculate(vector1, vector2);
        
        // Test with larger distance
        StringPrimitiveStatement stmt3 = new StringPrimitiveStatement(testCase, "elephant");
        vector2.put("var1", stmt3);
        
        double result2 = distance.calculate(vector1, vector2);
        assertTrue("Larger Levenshtein distance should result in larger calculated distance", result2 > result1);
    }

    @Test
    public void testCalculateWithIntegerValues() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        IntPrimitiveStatement stmt1 = new IntPrimitiveStatement(testCase, 10);
        IntPrimitiveStatement stmt2 = new IntPrimitiveStatement(testCase, 15);
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result = distance.calculate(vector1, vector2);
        assertTrue("Different integers should have positive distance", result > 0.0);
        
        // Test identical integers
        IntPrimitiveStatement stmt3 = new IntPrimitiveStatement(testCase, 10);
        vector2.put("var1", stmt3);
        
        double result2 = distance.calculate(vector1, vector2);
        assertEquals("Identical integers should have zero distance", 0.0, result2, 0.001);
    }

    @Test
    public void testCalculateWithDoubleValues() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        DoublePrimitiveStatement stmt1 = new DoublePrimitiveStatement(testCase, 3.14);
        DoublePrimitiveStatement stmt2 = new DoublePrimitiveStatement(testCase, 2.71);
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result = distance.calculate(vector1, vector2);
        assertTrue("Different doubles should have positive distance", result > 0.0);
        
        // Test identical doubles
        DoublePrimitiveStatement stmt3 = new DoublePrimitiveStatement(testCase, 3.14);
        vector2.put("var1", stmt3);
        
        double result2 = distance.calculate(vector1, vector2);
        assertEquals("Identical doubles should have zero distance", 0.0, result2, 0.001);
    }

    @Test
    public void testCalculateWithNullValues() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, null);
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, null);
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        
        double result = distance.calculate(vector1, vector2);
        assertEquals("Two null values should have zero distance", 0.0, result, 0.001);
        
        // Test null vs non-null
        StringPrimitiveStatement stmt3 = new StringPrimitiveStatement(testCase, "test");
        vector2.put("var1", stmt3);
        
        double result2 = distance.calculate(vector1, vector2);
        assertTrue("Null vs non-null should have positive distance", result2 > 0.0);
    }

    @Test
    public void testCalculateWithMultipleVariables() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        // Add multiple variables of different types
        StringPrimitiveStatement str1 = new StringPrimitiveStatement(testCase, "hello");
        StringPrimitiveStatement str2 = new StringPrimitiveStatement(testCase, "world");
        IntPrimitiveStatement int1 = new IntPrimitiveStatement(testCase, 42);
        IntPrimitiveStatement int2 = new IntPrimitiveStatement(testCase, 24);
        
        vector1.put("stringVar", str1);
        vector1.put("intVar", int1);
        vector2.put("stringVar", str2);
        vector2.put("intVar", int2);
        
        double result = distance.calculate(vector1, vector2);
        assertTrue("Multiple different variables should have positive distance", result > 0.0);
        
        // Compare to single variable case
        Map<String, PrimitiveStatement<?>> singleVector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> singleVector2 = new HashMap<>();
        singleVector1.put("stringVar", str1);
        singleVector2.put("stringVar", str2);
        
        double singleResult = distance.calculate(singleVector1, singleVector2);
        assertTrue("Multiple variables should have larger distance than single variable", result > singleResult);
    }

    @Test
    public void testCalculateWithMismatchedVariables() throws Exception {
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        // Variables that don't match between vectors
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "test1");
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "test2");
        
        vector1.put("var1", stmt1);
        vector2.put("var2", stmt2); // Different key
        
        double result = distance.calculate(vector1, vector2);
        assertEquals("Mismatched variables should have zero distance", 0.0, result, 0.001);
    }

    @Test
    public void testNormalizationFunction() throws Exception {
        // Test that the normalization function works correctly
        // by comparing different distance magnitudes
        
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector3 = new HashMap<>();
        
        StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "a");
        StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "ab"); // distance 1
        StringPrimitiveStatement stmt3 = new StringPrimitiveStatement(testCase, "abcdef"); // distance 5
        
        vector1.put("var1", stmt1);
        vector2.put("var1", stmt2);
        vector3.put("var1", stmt3);
        
        double smallDistance = distance.calculate(vector1, vector2);
        double largeDistance = distance.calculate(vector1, vector3);
        
        // Both should be normalized to 0-1 range per variable
        assertTrue("Small distance should be normalized to 0-1 range", smallDistance >= 0.0 && smallDistance <= 1.0);
        assertTrue("Large distance should be normalized to 0-1 range", largeDistance >= 0.0 && largeDistance <= 1.0);
        assertTrue("Larger actual distance should result in larger normalized distance", largeDistance > smallDistance);
    }

    @Test
    public void testWeightingSystem() throws Exception {
        // Test that string/numeric types are weighted more heavily than enums
        // This is implicit in the current implementation through the WEIGHT_STRING_NUMERIC constant
        
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        StringPrimitiveStatement str1 = new StringPrimitiveStatement(testCase, "test1");
        StringPrimitiveStatement str2 = new StringPrimitiveStatement(testCase, "test2");
        
        vector1.put("var1", str1);
        vector2.put("var1", str2);
        
        double stringDistance = distance.calculate(vector1, vector2);
        assertTrue("String distance should be weighted appropriately", stringDistance > 0.0);
    }

    @Test
    public void testLevenshteinDistanceDirectly() {
        // Test the internal Levenshtein implementation indirectly through string comparisons
        Map<String, PrimitiveStatement<?>> vector1 = new HashMap<>();
        Map<String, PrimitiveStatement<?>> vector2 = new HashMap<>();
        
        try {
            // Test case-insensitive comparison (Levenshtein converts to lowercase)
            StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "Hello");
            StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "hello");
            
            vector1.put("var1", stmt1);
            vector2.put("var1", stmt2);
            
            double result = distance.calculate(vector1, vector2);
            assertEquals("Case differences should be ignored", 0.0, result, 0.001);
            
            // Test with actual differences
            StringPrimitiveStatement stmt3 = new StringPrimitiveStatement(testCase, "world");
            vector2.put("var1", stmt3);
            
            double result2 = distance.calculate(vector1, vector2);
            assertTrue("Different strings should have positive distance", result2 > 0.0);
            
        } catch (Exception e) {
            fail("Should not throw exception during string comparison: " + e.getMessage());
        }
    }
}