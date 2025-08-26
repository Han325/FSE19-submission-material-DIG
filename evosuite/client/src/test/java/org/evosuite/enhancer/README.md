# EvoSuite Enhancer Tests

This directory contains comprehensive test cases for the EvoSuite enhancer functionality, which provides LLM-enhanced test input generation and genetic improvement capabilities.

## Test Structure

### Core Test Classes

1. **ContextExtractorTest.java**
   - Tests the `ContextExtractor` class functionality
   - Verifies extraction of semantic variable contexts from test cases
   - Tests handling of different statement types (EnumPrimitive, MethodStatement)
   - Includes edge cases like empty test cases, null values, and recursive extraction

2. **VariableUsageContextTest.java**
   - Tests the `VariableUsageContext` data container class
   - Verifies correct storage and retrieval of context information
   - Tests immutability and edge cases like null values and special characters
   - Includes performance tests with large data sets

3. **GeneticImproverTest.java**
   - Tests the `GeneticImprover` class for population diversification
   - Verifies handling of different population sizes and variation counts
   - Tests thread safety and consistency
   - Includes edge cases like empty populations and large variation requests

4. **LLMInputEnhancerTest.java**
   - Tests the main `LLMInputEnhancer` functionality
   - Uses `MockLLMClient` to avoid external dependencies
   - Tests JSON response parsing and error handling
   - Verifies integration with context extraction

5. **distance/InputVectorDistanceTest.java**
   - Tests the distance calculation functionality
   - Verifies string distance calculation using Levenshtein algorithm
   - Tests normalization and weighting systems
   - Includes tests for different primitive types (String, Integer, Double)

### Support Classes

6. **MockLLMClient.java**
   - Mock implementation of `LLMClient` interface for testing
   - Provides configurable responses, failure simulation, and call tracking
   - Supports simulated delays and response customization

7. **EnhancerTestSuite.java**
   - JUnit test suite that runs all enhancer tests together
   - Convenient entry point for running the complete test battery

## Running the Tests

### Individual Test Classes
```bash
# Run a specific test class
mvn test -Dtest=ContextExtractorTest
mvn test -Dtest=VariableUsageContextTest
mvn test -Dtest=GeneticImproverTest
mvn test -Dtest=LLMInputEnhancerTest
mvn test -Dtest=InputVectorDistanceTest
```

### Complete Test Suite
```bash
# Run all enhancer tests together
mvn test -Dtest=EnhancerTestSuite
```

### All Tests
```bash
# Run all tests in the project
mvn test
```

## Test Coverage

The test suite covers:

- **Core functionality**: Basic operations of each class
- **Edge cases**: Empty inputs, null values, boundary conditions
- **Error handling**: Malformed inputs, exception scenarios
- **Integration**: Interaction between different components
- **Performance**: Basic performance characteristics
- **Thread safety**: Concurrent usage scenarios
- **Mocking**: External dependency isolation

## Dependencies

The tests use the following frameworks and libraries:
- JUnit 4 for test framework
- Mockito for mocking (where needed)
- JSON library for JSON parsing tests
- EvoSuite core classes for integration testing

## Test Design Principles

1. **Isolation**: Each test is independent and can run in any order
2. **Mocking**: External dependencies (like LLM services) are mocked
3. **Coverage**: Both positive and negative test cases are included
4. **Performance**: Tests are designed to run quickly
5. **Maintainability**: Clear naming and documentation for easy maintenance

## Adding New Tests

When adding new enhancer functionality:

1. Create corresponding test classes following the naming convention `<ClassName>Test.java`
2. Add the test class to the `EnhancerTestSuite`
3. Follow the existing patterns for setup, teardown, and assertions
4. Include both positive and negative test cases
5. Mock external dependencies where appropriate
6. Update this README if needed

## Known Limitations

- Some tests may require specific EvoSuite internal structures that are not easily mockable
- Integration tests are limited by the complexity of creating realistic test scenarios
- Performance tests provide basic checks but are not comprehensive benchmarks
- Thread safety tests are basic and may not catch all concurrency issues

## Troubleshooting

If tests fail:

1. Check that all dependencies are properly configured
2. Verify that mock objects are set up correctly
3. Ensure test data meets the expected format requirements
4. Check for any changes in the underlying EvoSuite classes that might affect tests
5. Review test logs for specific error messages and stack traces