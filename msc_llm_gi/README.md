# MSc SSE Individual Research Project Submission Details

This markdown document contains the details of the code artifiact submissions made for the MSc research project.

## About
This repository contains the all the proposed added evaluation metrics done as per the research report. The additions are made in the `code-coverage` branch from the previous DIG author which already includes the branch coverage metric setup.

The following sections will detail what has been added, and how it is used.

--- 

## Code Coverage Analysis System Tool Enhancements

The additions/modifications are done in the `code-coverage` directory that is located in `workspace/codecoverage`, additional files that are not based in the `code-coverage` directory are located in `msc_llm_gi` folder itself.

---

### Files Added and Technical Description

#### Coverage Analysis Scripts

**`calculate-auc.sh`**
- Calculates Area Under the Curve (AUC) for branch coverage progression over time
- Uses trapezoidal rule to measure coverage improvement across test execution
- Parses coverage report files to extract branch coverage percentages (4th field, removes % symbols)
- Outputs "coverage-percent-seconds" metric for time-series analysis
- Includes debugging output showing file processing and timestamp extraction

#### Deployment Infrastructure

**`run.sh` (Modified)**
- Replaced legacy Docker container approach with Docker Compose integration
- Added `runContainer()` function using `docker compose up -d` for service orchestration
- Added `stopCompose()` function with `docker compose down --remove-orphans` for cleanup
- Enhanced validation to check Docker Compose service status instead of individual containers
- Maintains backward compatibility with existing port checking and project validation

#### Coverage Data Collection

**`CoverageManager.java` (Modified)**
- **Added Method**: `getCoverageJsonObjectAsString()` - Retrieves raw coverage JSON from Express server endpoint `/coverage/object`
- **Added Method**: `writeRawJsonCoverageReport()` - Writes raw JSON coverage data to `coverage-final.json`
- **Integration**: Methods called in `@AfterClass` teardown to export complete coverage data for external analysis

**`TestExecution.java` (Modified)**
- **Modified**: `@AfterClass` method to include coverage JSON extraction calls
- Added `LogExporter.initialize()` and `LogExporter.exportCollectedLogsToFile()` integration
- Enhanced teardown sequence: JSON export → coverage reset → driver cleanup

#### Error Collection and Analysis

**`LogExporter.java` (Added)**
- **Core Method**: `processLogsAfterTest()` - Called after each individual test case
  - Extracts browser console logs (SEVERE level errors)
  - Maintains cumulative set of unique error messages
  - Writes fault discovery rate to `fault_discovery_rate.csv` on Desktop
  - Tracks incremental fault discovery across test suite execution
- **Export Method**: `exportCollectedLogsToFile()` - Called once at test suite completion
  - Exports all raw browser logs to `raw_browser_logs.txt`
  - Exports deduplicated SEVERE faults to `unique_faults.txt`
  - Both files saved to Desktop for analysis
- **Initialization**: `initialize()` - Resets counters and clears previous CSV files

#### Fault Discovery Analytics

**`plot_discovery_curve.py` (Added)**
- Calculates fault discovery efficiency score using AUC-style metric
- Compares actual fault discovery curve against "perfect" discovery scenario
- Uses trapezoidal rule for area calculation under fault discovery curve
- Generates `discovery_curve.png` visualization with discovery score in title
- Includes comprehensive debugging output with step-by-step calculation breakdown
- Score ranges 0-1.0 where 1.0 = perfect test suite (all faults found on first test)

---

## Integration Flow

1. **Test Execution**: `run.sh` orchestrates Docker Compose services and test suite execution
2. **Per-Test Collection**: `LogExporter.processLogsAfterTest()` tracks fault discovery rates
3. **Suite Completion**: `CoverageManager` exports raw JSON + `LogExporter` exports comprehensive logs
4. **Post-Processing**: `calculate-auc.sh` computes coverage AUC from report files
5. **Analysis**: `plot_discovery_curve.py` generates fault discovery metrics and visualization

## Output Files

- `coverage-report*.txt` - Intermediate coverage reports (parsed by calculate-auc.sh)
- `coverage-final.json` - Raw coverage JSON data for external analysis
- `fault_discovery_rate.csv` - Incremental fault discovery rates (Desktop)
- `raw_browser_logs.txt` - Complete browser console logs (Desktop)
- `unique_faults.txt` - Deduplicated SEVERE error messages (Desktop)
- `discovery_curve.png` - Fault discovery visualization with efficiency score (Desktop)

## Key Metrics Generated

- **Coverage AUC**: Time-integrated branch coverage progression ("coverage-percent-seconds")
- **Fault Discovery Score**: Efficiency metric comparing actual vs perfect fault discovery (0-1.0 scale)
- **Incremental Fault Discovery**: CSV tracking unique fault accumulation per test case
- **Comprehensive Error Analysis**: Raw logs + unique fault extraction for detailed debugging

## Technical Dependencies

- Docker Compose for service orchestration
- Java Selenium WebDriver for browser automation
- Express.js server for coverage aggregation
- Istanbul instrumentation for JavaScript coverage collection
- Python matplotlib/pandas for discovery curve analysis

## VM Setup Instructions
All the additions are ran in a VM that contains all the files and dependencies needed. Once you have the `Code Coverage`(baseline)/`Code Coverage Enhanced`(Enhanced, duh) VM setup in your `UTM` or equivalent VM runner, please follow the commands listed in `msc_llm_gi/commands.md` to run the experiment. 