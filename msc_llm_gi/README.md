# MSc SSE Individual Research Project Submission Details

This markdown document contains the details of the code artifiact submissions made for the MSc research project.

## About
This repository contains the all the proposed LLM x GI Input Diversification work done as per the research report. As detailed in the report, the additions are done towards Evosuite, since DIG is implemented within Evosuite. However, this does mean that Evosuite core functionality is modified, but extended to be precise. 

The following sections will detail what has been added, and how it is used.

--- 

## LLM x GI Enhancement Additions

All LLM x GI additions are located in the newly added `enhancer` folder which is located in `evosuite/client/src/main/java/org/evosuite/enhancer`

## Files Added and Technical Description

#### Core Enhancement Pipeline

**`LLMInputEnhancer.java`**
- Main orchestrator that takes EvoSuite-generated test cases and enhances them using LLM suggestions
- Extracts semantic variables from test cases, sends them to LLM for better values, applies modifications surgically
- Supports two modification strategies: setValue() for simple enums, "Smart Duplication" for factory-created objects
- Uses a planned modification approach to prevent infinite loops during test case modification

**`ContextExtractor.java`**
- Identifies "semantic variables" in test cases (custom classes starting with "custom_classes.")
- Filters out blacklisted types and focuses on variables created via EnumPrimitiveStatement or MethodStatement
- Traces back through method calls (like `fromString()`) to find the original primitive values
- Returns contexts containing variable info, usage patterns, and original values

**`VariableUsageContext.java`**
- Data container holding context about semantic variables: the variable reference, declaration statement, original primitive value, method usage list, and usage count
- Used to pass information between extraction and enhancement phases

**`Modification.java`**
- Simple POJO representing a planned modification (context + suggested value)
- Decouples LLM decision-making from actual test case modification to prevent listener-based infinite loops

#### LLM Client Layer

**`LLMClient.java`**
- Interface for LLM communication, abstracts away specific providers

**`OllamaClient.java`**
- Implementation for local Ollama models
- Uses HttpURLConnection for HTTP requests to Ollama API (default: localhost:11434)
- Configurable host IP for different network setups

**`OpenAIClient.java`**
- Implementation for OpenAI API
- Converts generic JSON requests to OpenAI's message format
- Requires OPENAI_API_KEY environment variable

#### Genetic Improvement Phase

**`GeneticImprover.java`**
- Takes LLM-enhanced seeds and generates diverse variations using mutation strategies
- Creates "audition pools" of candidate mutations, selects most diverse ones using distance metrics
- Implements "Plausible Diversity" mutations: numeric deltas, word swaps, typos, string manipulations
- Uses self-sourcing dictionary (extracts words from existing test strings for realistic mutations)

**`distance/InputVectorDistance.java`**
- Calculates weighted distance between test variations for diversity selection
- Uses Levenshtein distance for strings, absolute difference for numbers
- Applies weights to prioritize complex mutations over simple enum flips

#### Logging and Debugging

**`DebugStoryLogger.java`**
- Comprehensive human-readable logging of the entire enhancement process
- Writes detailed "story" logs to Desktop with timestamps
- Tracks LLM prompts, responses, decisions, modifications, and outcomes
- Includes performance timing for both LLM and GI phases

**`PromptLogger.java`**
- Dedicated logger for LLM prompts only
- Saves exact JSON payloads sent to LLM for external analysis and debugging

### Integration Point

**`AdaptiveRandomSearch.java` (Modified)**
- **Key Integration**: Enhancement system plugged into the `evolve()` method
- **Two-Phase Process**:
  1. Generate k candidates → LLM enhance each candidate → measure timing
  2. Pass enhanced seeds to GeneticImprover → generate variations → select diverse family
- **Seed Banking**: Optional modes to collect high-quality seeds or reuse pre-collected seeds
- **Performance Tracking**: Times both LLM and GI phases, logs to DebugStoryLogger

## Integration Point

The integration requires a slight modification to the `AdaptiveRandomSearch.java` which contains the core DIG functionality and is located in `evosuite/client/src/main/java/org/evosuite/ga/metaheuristics/art`

#### Modifications/Integration in AdaptiveRandomSearch:
```java
// Phase A: LLM Enhancement
for (int i = 0; i < k; i++) {
    T candidate = chromosomeFactory.getChromosome();
    this.llmEnhancer.enhanceCandidate((TestChromosome) candidate);
    candidates.add(candidate);
}

// Phase B: Genetic Improvement  
candidates = (List<T>) this.geneticImprover.diversifyPopulation(
    (List<TestChromosome>) candidates, variationsPerSeed);
```

### Configuration

#### Environment Variables
- `LLM_PROVIDER`: "ollama" (default) or "openai"
- `LLM_MODEL`: Model name (defaults: "qwen2.5:7b" for Ollama, "gpt-4o-mini" for OpenAI)
- `OPENAI_API_KEY`: Required for OpenAI client

#### Master Switches
- `DRY_RUN`: Skip actual modifications (in LLMInputEnhancer)
- `SEED_COLLECTION_MODE`: Collect enhanced seeds to seed_bank.ser
- `USE_SEED_BANK_MODE`: Use pre-collected seeds instead of generating new ones
- `DEEP_DIVE_MODE`: Debug mode in GeneticImprover (disabled for production)

#### Tunable Parameters
- `AUDITION_POOL_SIZE`: Number of mutation candidates to generate (default: 50)
- `MAX_MUTATIONS_PER_CANDIDATE`: Maximum mutations per test case (default: 3)
- `variationsPerSeed`: Diverse variations per LLM-enhanced seed (default: 5)

### File Outputs

- `llm_debug_story_TIMESTAMP.log`: Detailed enhancement process log (Desktop)
- `llm_prompts_TIMESTAMP.log`: LLM request payloads only
- `seed_bank.ser`: Serialized seed collection (Desktop, when SEED_COLLECTION_MODE=true)

### Key Data Flow

1. **EvoSuite generates random test case**
2. **ContextExtractor** finds semantic variables in custom_classes.*
3. **LLMInputEnhancer** creates prompts with variable context
4. **LLM** suggests better values based on type and usage patterns
5. **Surgical modification** applied to test case (setValue or Smart Duplication)
6. **GeneticImprover** generates diverse mutations of enhanced seeds
7. **Distance-based selection** picks most diverse family members
8. **AdaptiveRandomSearch** continues with enhanced, diverse population

The system transforms EvoSuite's random primitive values into semantically meaningful, diverse test inputs for Page Object Model testing.

## Modifications done to the benchmarks

As detailed in the research report, some modifications are done to the `custom_classes` for the benchmarks applications selected. The files modified are located in these directories:

`fse2019/dimeshift/src/main/java/custom_classes`
`fse2019/splittypie/src/main/java/custom_classes`
`fse2019/retroboard/src/main/java/custom_classes`

## VM Setup Instructions
All the additions are ran in a VM that contains all the files and dependencies needed. Once you have the `ENHANCED-DIGS` file setup in your `UTM` or equivalent VM runner, please follow the commands listed in `msc_llm_gi/commands.md` to run the experiment. 

## Prerequisites
Please have Ollama installed, and have `qwen2.5:7b` model loaded to have the LLM functionality working.