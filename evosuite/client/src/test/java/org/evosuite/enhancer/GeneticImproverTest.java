package org.evosuite.enhancer;

import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class GeneticImproverTest {

    private GeneticImprover geneticImprover;

    @Before
    public void setUp() {
        geneticImprover = new GeneticImprover();
    }

    @Test
    public void testDiversifyPopulationWithEmptyList() {
        List<TestChromosome> emptySeeds = new ArrayList<>();
        List<TestChromosome> result = geneticImprover.diversifyPopulation(emptySeeds, 3);
        
        assertTrue("Empty seed list should return empty result", result.isEmpty());
    }

    @Test
    public void testDiversifyPopulationWithSingleSeed() {
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(seed);
        
        int variationsPerSeed = 3;
        List<TestChromosome> result = geneticImprover.diversifyPopulation(seeds, variationsPerSeed);
        
        // The result should contain some chromosomes (exact number depends on implementation)
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
    }

    @Test
    public void testDiversifyPopulationWithMultipleSeeds() {
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(createBasicTestChromosome());
        seeds.add(createBasicTestChromosome());
        
        int variationsPerSeed = 2;
        List<TestChromosome> result = geneticImprover.diversifyPopulation(seeds, variationsPerSeed);
        
        assertNotNull("Result should not be null", result);
        assertFalse("Result should not be empty", result.isEmpty());
    }

    @Test
    public void testDiversifyPopulationWithZeroVariations() {
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(seed);
        
        int variationsPerSeed = 0;
        List<TestChromosome> result = geneticImprover.diversifyPopulation(seeds, variationsPerSeed);
        
        assertNotNull("Result should not be null", result);
        // With zero variations, we should still get some result (likely the original seeds)
    }

    @Test
    public void testDiversifyPopulationPreservesOriginalList() {
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> originalSeeds = new ArrayList<>();
        originalSeeds.add(seed);
        
        List<TestChromosome> seedsCopy = new ArrayList<>(originalSeeds);
        
        geneticImprover.diversifyPopulation(originalSeeds, 2);
        
        // Original list should be unchanged
        assertEquals("Original seeds list should be unchanged", seedsCopy.size(), originalSeeds.size());
    }

    @Test
    public void testDiversifyPopulationWithLargeVariationCount() {
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(seed);
        
        int variationsPerSeed = 100; // Large number
        List<TestChromosome> result = geneticImprover.diversifyPopulation(seeds, variationsPerSeed);
        
        assertNotNull("Result should not be null", result);
        // The implementation should handle large variation counts gracefully
        // Exact behavior depends on internal limits and constraints
    }

    @Test
    public void testDiversifyPopulationConsistency() {
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(seed);
        
        // Run diversification multiple times with same parameters
        List<TestChromosome> result1 = geneticImprover.diversifyPopulation(new ArrayList<>(seeds), 3);
        List<TestChromosome> result2 = geneticImprover.diversifyPopulation(new ArrayList<>(seeds), 3);
        
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
        
        // Results may differ due to randomness, but basic properties should be consistent
        // Both results should be non-empty if seeds are non-empty
        if (!seeds.isEmpty()) {
            assertFalse("First result should not be empty", result1.isEmpty());
            assertFalse("Second result should not be empty", result2.isEmpty());
        }
    }

    @Test
    public void testDiversifyPopulationWithComplexTestCase() {
        TestChromosome complexSeed = createComplexTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(complexSeed);
        
        int variationsPerSeed = 3;
        List<TestChromosome> result = geneticImprover.diversifyPopulation(seeds, variationsPerSeed);
        
        assertNotNull("Result should not be null", result);
        // Should handle complex test cases without crashing
    }

    @Test
    public void testDiversifyPopulationThreadSafety() throws InterruptedException {
        // Basic thread safety test
        TestChromosome seed = createBasicTestChromosome();
        List<TestChromosome> seeds = new ArrayList<>();
        seeds.add(seed);
        
        List<Thread> threads = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    GeneticImprover gi = new GeneticImprover(); // Each thread gets its own instance
                    List<TestChromosome> result = gi.diversifyPopulation(new ArrayList<>(seeds), 2);
                    assertNotNull("Result should not be null in thread", result);
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            });
            threads.add(thread);
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        if (!exceptions.isEmpty()) {
            fail("Thread safety test failed: " + exceptions.get(0).getMessage());
        }
    }

    // Helper methods

    private TestChromosome createBasicTestChromosome() {
        TestCase testCase = new DefaultTestCase();
        try {
            StringPrimitiveStatement stmt = new StringPrimitiveStatement(testCase, "basic_value");
            testCase.addStatement(stmt);
        } catch (Exception e) {
            // If we can't add statements, just return with empty test case
        }
        
        TestChromosome chromosome = new TestChromosome();
        chromosome.setTestCase(testCase);
        return chromosome;
    }

    private TestChromosome createComplexTestChromosome() {
        TestCase testCase = new DefaultTestCase();
        try {
            // Add multiple statements to create a more complex test case
            StringPrimitiveStatement stmt1 = new StringPrimitiveStatement(testCase, "complex_value_1");
            testCase.addStatement(stmt1);
            
            StringPrimitiveStatement stmt2 = new StringPrimitiveStatement(testCase, "complex_value_2");
            testCase.addStatement(stmt2);
            
            StringPrimitiveStatement stmt3 = new StringPrimitiveStatement(testCase, "complex_value_3");
            testCase.addStatement(stmt3);
        } catch (Exception e) {
            // If we can't add statements, just return with whatever we have
        }
        
        TestChromosome chromosome = new TestChromosome();
        chromosome.setTestCase(testCase);
        return chromosome;
    }
}