import pandas as pd
import matplotlib.pyplot as plt
import os

# Get the path to the user's Desktop
desktop_path = os.path.join(os.path.expanduser('~'), 'Desktop')
csv_path = os.path.join(desktop_path, 'fault_discovery_rate.csv')
png_path = os.path.join(desktop_path, 'discovery_curve.png')

print("--- STARTING DIAGNOSTIC RUN ---")
print(f"### STEP 1: Reading CSV file from: {csv_path}")

try:
    data = pd.read_csv(csv_path)
except FileNotFoundError:
    print(f"FUCK! The file '{csv_path}' was not found. Run the Java tests first.")
    exit()

if data.empty:
    print("FUCK! The CSV file is empty. No data to process.")
    exit()

# --- THIS IS THE MOTHERFUCKING DEBUGGING SECTION ---
print("\n### STEP 2: RAW DATA FROM CSV ###")
print("This is the ENTIRE content of the CSV file:")
print(data.to_string())
print("---------------------------------\n")

max_faults = data['cumulative_unique_faults'].max()
max_tests = data['test_case_number'].max()

print("### STEP 3: KEY VARIABLES ###")
print(f"Max faults found (max_faults): {max_faults}")
print(f"Total number of tests (max_tests): {max_tests}")
print("-----------------------------\n")

auc_style_score = 0.0
if max_faults > 0 and max_tests > 1:
    actual_area = 0.0
    print("### STEP 4: AREA CALCULATION LOOP ###")
    print("Calculating the area under the curve piece by piece...")
    # Loop from the second data point to the end
    for i in range(1, len(data)):
        # We need to get the row for the current and previous test case
        # pandas uses .iloc[] for integer-location based indexing
        faults_current = data['cumulative_unique_faults'].iloc[i]
        faults_previous = data['cumulative_unique_faults'].iloc[i-1]
        
        # Area of a trapezoid: (base1 + base2) * height / 2. Here height is 1.
        trapezoid_area = (faults_current + faults_previous) / 2.0
        actual_area += trapezoid_area
        
        print(f"  - Interval {i} (between test {i} and {i+1}):")
        print(f"    - Faults at end of previous test: {faults_previous}")
        print(f"    - Faults at end of current test:  {faults_current}")
        print(f"    - Area of this piece: ({faults_current} + {faults_previous}) / 2 = {trapezoid_area}")
        print(f"    - Running total for actual_area: {actual_area}")

    print("\n  >>> FINAL Calculated Area (actual_area):", actual_area)
    print("-----------------------------------\n")

    print("### STEP 5: 'PERFECT' SCENARIO CALCULATION ###")
    # The "perfect" case finds all faults on test #1 and stays flat.
    # The area of this perfect rectangle is max_faults * (number of intervals)
    perfect_area = max_faults * (max_tests - 1)
    print(f"Max faults ({max_faults}) * (Number of tests ({max_tests}) - 1) = {perfect_area}")
    print(f"  >>> 'Perfect' Area (perfect_area): {perfect_area}")
    print("------------------------------------------\n")

    auc_style_score = actual_area / perfect_area if perfect_area > 0 else 0
    print("### STEP 6: FINAL SCORE CALCULATION ###")
    print(f"  >>> Final Score = actual_area ({actual_area}) / perfect_area ({perfect_area}) = {auc_style_score}")
    print("-------------------------------------\n")
else:
    print("\nSKIPPING CALCULATION: Either no faults were found (max_faults=0) or there was only one test (max_tests<=1).")


# --- Plotting the Curve (no changes here) ---
plt.figure(figsize=(10, 6))
plt.plot(data['test_case_number'], data['cumulative_unique_faults'], marker='o', linestyle='-')
plt.title(f'Fault Discovery Rate Over Time\n(Discovery Score: {auc_style_score:.4f})')
plt.xlabel('Number of Test Cases Executed')
plt.ylabel('Cumulative Unique Faults Found')
plt.grid(True)
plt.xticks(range(0, max_tests + 1, max(1, max_tests // 10)))
plt.yticks(range(0, int(max_faults) + 2))
plt.xlim(left=0)
plt.ylim(bottom=0)
plt.tight_layout()
plt.savefig(png_path)

# --- Final Summary (no changes here) ---
print("\n--- Fault Discovery Analysis ---")
print(f"Analysis complete. Plot saved to '{png_path}'")
print(f"A score of 1.0 represents a perfect test suite (all faults found on the first test).")
print(f"Your Test Suite's Discovery Score: {auc_style_score:.4f}\n")