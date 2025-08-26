package org.evosuite.enhancer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all enhancer-related tests.
 * This provides a convenient way to run all enhancer tests together.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ContextExtractorTest.class,
    VariableUsageContextTest.class,
    GeneticImproverTest.class,
    LLMInputEnhancerTest.class,
    org.evosuite.enhancer.distance.InputVectorDistanceTest.class
})
public class EnhancerTestSuite {
    // This class remains empty, it is used only as a holder for the above annotations
}