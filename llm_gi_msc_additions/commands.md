# Code Coverage VMs Commands (Ports 2225 & 2226)

## SSH Connections
```bash
# Standard code coverage VM
ssh -p 2225 vagrant-code-coverage@localhost

# Enhanced code coverage VM
ssh -p 2226 vagrant-cc-enhanced@localhost
```

## File Transfers & Setup

### Code Coverage VM Setup (Port 2225)
```bash
# Transfer applications to code coverage VM
scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/splittypie/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/
scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/retroboard/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/
scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/dimeshift/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/
scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/codecoverage/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/
```

### Enhanced Code Coverage VM Setup (Port 2226)
```bash
# Transfer codecoverage tools to enhanced VM
scp -r -P 2226 ~/Documents/Code/code-coverage/workspace/codecoverage/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/

# Transfer updated applications to enhanced VM
scp -r -P 2226 ~/Documents/Code/cc-cut-new/splittypie/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/
scp -r -P 2226 ~/Documents/Code/cc-cut-new/retroboard/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/
scp -r -P 2226 ~/Documents/Code/cc-cut-new/dimeshift/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/
scp -r -P 2226 ~/Documents/Code/cc-cut-new/test-generation-results/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/

# Transfer custom classes for splittypie
scp -r -P 2226 ~/Documents/Code/dig/fse2019/splittypie/src/main/java/custom_classes/* vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/splittypie/src/main/java/custom_classes
```

## Test Execution Commands

### Standard Code Coverage VM (Port 2225) - Baseline Tests
```bash
# Splittypie tests
./run.sh splittypie \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa" \
         "/home/vagrant-code-coverage/workspace/fse2019/splittypie" \
         splittypie \
         3000 \
         0 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh splittypie \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/baseline" \
         "/home/vagrant-code-coverage/workspace/fse2019/splittypie" \
         splittypie \
         3000 \
         0 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

# Dimeshift tests (with MySQL on port 3306)
./run.sh dimeshift \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/mosa" \
         "/home/vagrant-code-coverage/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline" \
         "/home/vagrant-code-coverage/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline-digsi" \
         "/home/vagrant-code-coverage/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

# Retroboard tests
./run.sh retroboard \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/mosa" \
         "/home/vagrant-code-coverage/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh retroboard \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline" \
         "/home/vagrant-code-coverage/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh retroboard \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline-digsi" \
         "/home/vagrant-code-coverage/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014
```

## Mutation Testing

### Standard Code Coverage VM (Port 2225)

#### Splittypie Mutation Testing
```bash
# Standard mutation testing
./run_mutation_testing.sh \
    "/home/vagrant-code-coverage/Desktop/splittypie-mutant-patches" \
    "splittypie" \
    "/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa" \
    "/home/vagrant-code-coverage/workspace/fse2019/splittypie" \
    "splittypie" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
    7014   

# Mutation testing with original code backup
./run_mutation_testing.sh \
    "/home/vagrant-code-coverage/Desktop/splittypie-mutant-patches" \
    "splittypie" \
    "/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa" \
    "/home/vagrant-code-coverage/workspace/fse2019/splittypie" \
    "splittypie" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
    7014 \
    "/home/vagrant-code-coverage/Desktop/splittypie-og-code"  
```

#### Dimeshift Mutation Testing
```bash
# Baseline mutation testing with specific mutants
./run_mutation_testing_baseline.sh \
    "/home/vagrant-code-coverage/Desktop/dimeshift-mutant-patches" \
    "dimeshift" \
    "/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/7" \
    "/home/vagrant-code-coverage/workspace/fse2019/dimeshift" \
    "dimeshift" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
    7014 \
    ./mutants_to_rerun.txt
```

#### Retroboard Mutation Testing
```bash
# Baseline retroboard mutation testing
./run_mutation_testing_baseline.sh \
    "/home/vagrant-code-coverage/Desktop/retroboard-mutant-patches" \
    "retroboard" \
    "/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/4" \
    "/home/vagrant-code-coverage/workspace/fse2019/retroboard" \
    "retroboard" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
    7014
```

### Enhanced Code Coverage VM (Port 2226)

#### Dimeshift Enhanced Mutation Testing
```bash
# Enhanced mutation testing
./run_mutation_testing_enhanced.sh \
    "/home/vagrant-cc-enhanced/Desktop/dimeshift-mutant-patches" \
    "dimeshift" \
    "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/13" \
    "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
    "dimeshift" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
    7014 \
    ./mutants_to_rerun.txt
```

#### Retroboard Enhanced Mutation Testing
```bash
# Enhanced retroboard mutation testing
./run_mutation_testing_enhanced.sh \
    "/home/vagrant-cc-enhanced/Desktop/retroboard-mutant-patches" \
    "retroboard" \
    "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/15" \
    "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
    "retroboard" \
    3000 \
    0 \
    4444 \
    "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
    7014
```
```bash
# Splittypie enhanced tests
./run.sh splittypie \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/mosa" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/splittypie" \
         splittypie \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh splittypie \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/llmgi" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/splittypie" \
         splittypie \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014  

./run.sh splittypie \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/gitest" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/splittypie" \
         splittypie \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014   

# Dimeshift enhanced tests
./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/mosa" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/12" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi-safe" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/1" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

# Retroboard enhanced tests
./run.sh retroboard \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/mosa" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh retroboard \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

./run.sh retroboard \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi-long" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014
```

## Results Collection

### From Code Coverage VMs
```bash
# Standard code coverage results (Port 2225)
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/Desktop/*' ~/Downloads/testing-faults

# Specific test results by application and method (Port 2225)
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa/testsplittypieMosa_0' ~/Downloads/modified-code-coverage
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/baseline/testsplittypieBaseline_0' ~/Downloads/cc-baseline/
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline/testdimeshiftBase_0' ~/Downloads/cc-baseline/
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline-digsi/testdimeshiftBase_0' ~/Downloads/cc-baseline/
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline/testretroboardBase_0' ~/Downloads/retroboard-cc-results/
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline-digsi/testretroboardBase_0' ~/Downloads/retroboard-cc-results/
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/mosa/testdimeshiftMosa_0' ~/Downloads/cc-baseline
scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/mosa/testretroboardMosa_0' ~/Downloads/cc-baseline

# Enhanced VM results (Port 2226)
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/Desktop/*' ~/Downloads/testing-faults

# Specific enhanced test results (Port 2226)
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/mosa/testsplittypieMosa_0' ~/Downloads/modified-code-coverage
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/llmgi/testsplittypieLLM_0' ~/Downloads/cc-investigation
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/gitest/testsplittypieGiTest_0' ~/Downloads/cc-broken-json
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/mosa/testdimeshiftMosa_0' ~/Downloads/cc-baseline
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi/testdimeshiftLLM_0' ~/Downloads/dimeshift
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi-safe/testdimeshiftLLM_0' ~/Downloads/dimeshift-cc-safe
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/mosa/testretroboardMosa_0' ~/Downloads/cc-baseline
scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi-long/testretroboardLLM_0' ~/Downloads/
```

## Special File Transfers

### Test Data Transfers
```bash
# Transfer baseline official test data (to port 2225)
scp -r -P 2225 ~/Downloads/baseline-official/testsplittypieAdaptiveSequence_0/main/* vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/baseline/testsplittypieBaseline_0/main

# Transfer evolved test data with capabilities disabled (to port 2226)
scp -r -P 2226 ~/Downloads/evolve-cap-disabled/testsplittypieAdaptiveSequence_0/main/* vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/llmgi/testsplittypieLLM_0/main
```

## VM Information
- **Port 2225**: Standard code coverage VM (vagrant-code-coverage@localhost)
- **Port 2226**: Enhanced code coverage VM (vagrant-cc-enhanced@localhost)

## Test Generation Methods
- **mosa**: Multi-Objective Search Algorithm
- **baseline**: Standard baseline approach
- **baseline-digsi**: Baseline with DIGSI enhancements
- **llmgi**: Large Language Model Guided Integration
- **llmgi-safe**: Safe variant of LLM guided approach
- **llmgi-long**: Extended LLM guided approach
- **gitest**: Git-based test approach

## Application Details
- **Splittypie**: Bill splitting application (no database)
- **Dimeshift**: Shift management application (requires MySQL on port 3306)
- **Retroboard**: Retrospective board application (no database)

## Common Parameters
- **Port 3000**: Application server port
- **Port 4444**: WebDriver port
- **Port 7014**: Code coverage server port
- **Port 3306**: MySQL database port (for dimeshift only)