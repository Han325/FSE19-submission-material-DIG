# QUICK COMMANDS FOR SETUP

```
OLLAMA_HOST=0.0.0.0 ollama serve
```

```
ssh -i ~/.ssh/id_utm_vm -p 2222 vagrant@localhost
```

ssh -p 2224 vagrant-old@localhost

```
scp -r -P 2222 ~/.m2/repository/org/evosuite/ vagrant@localhost:/home/vagrant/.m2/repository/org/
```

```
scp -r -P 2222 ~/Documents/Code/dig/fse2019/dimeshift/ vagrant@localhost:/home/vagrant/workspace/fse2019/
```

```
scp -r -P 2222 ~/Documents/Code/dig/fse2019/petclinic/ vagrant@localhost:/home/vagrant/workspace/fse2019/
```

```
scp -r -P 2222 ~/Documents/Code/dig/fse2019/splittypie/ vagrant@localhost:/home/vagrant/workspace/fse2019/
```


```
scp -r -P 2222 ~/Documents/Code/dig/fse2019/retroboard/ vagrant@localhost:/home/vagrant/workspace/fse2019/
```

```
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/gi-wip
```
scp -r -P 2224 'vagrant-old@localhost:/home/vagrant-old/Desktop/*' ~/Downloads/baseline-rerun

scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/dimeshift/initial

scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/petclinic/initial

scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/splittypie/initial

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

ssh -p 2225 vagrant-code-coverage@localhost

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/splittypie/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/retroboard/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/fse2019/dimeshift/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/fse2019/

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/codecoverage/ vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/

scp -r -P 2226 ~/Documents/Code/code-coverage/workspace/codecoverage/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/Desktop/*' ~/Downloads/testing-faults

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa/testsplittypieMosa_0' ~/Downloads/modified-code-coverage

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/baseline/testsplittypieBaseline_0' ~/Downloads/cc-baseline/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline/testdimeshiftBase_0' ~/Downloads/cc-baseline/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/baseline-digsi/testdimeshiftBase_0' ~/Downloads/cc-baseline/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline/testretroboardBase_0' ~/Downloads/retroboard-cc-results/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/baseline-digsi/testretroboardBase_0' ~/Downloads/retroboard-cc-results/

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/mosa/testdimeshiftMosa_0' ~/Downloads/cc-baseline

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/mosa/testretroboardMosa_0' ~/Downloads/cc-baseline


scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/plot_discovery_curve.py vagrant-code-coverage@localhost:/home/vagrant-code-coverage/Desktop/

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/codecoverage/calculate-auc.sh vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/codecoverage

git push fork code-coverage

scp -r -P 2225 ~/Downloads/baseline-official/testsplittypieAdaptiveSequence_0/main/* vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/baseline/testsplittypieBaseline_0/main


scp -r -P 2226 ~/Downloads/evolve-cap-disabled/testsplittypieAdaptiveSequence_0/main/* vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/llmgi/testsplittypieLLM_0/main

scp -r -P 2226 ~/Documents/Code/dig/fse2019/splittypie/src/main/java/custom_classes/* vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/splittypie/src/main/java/custom_classes


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
    7014 \


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
    7014 \
 

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

./run.sh retroboard \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi-long" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014

ssh -p 2226 vagrant-cc-enhanced@localhost

scp -r -P 2226 ~/Documents/Code/cc-cut-new/splittypie/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/

scp -r -P 2226 ~/Documents/Code/cc-cut-new/retroboard/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/

scp -r -P 2226 ~/Documents/Code/cc-cut-new/dimeshift/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/fse2019/

scp -r -P 2226 ~/Documents/Code/cc-cut-new/test-generation-results/ vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/Desktop/*' ~/Downloads/testing-faults

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/mosa/testsplittypieMosa_0' ~/Downloads/modified-code-coverage

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/llmgi/testsplittypieLLM_0' ~/Downloads/cc-investigation

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsSplittypie/gitest/testsplittypieGiTest_0' ~/Downloads/cc-broken-json

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/mosa/testdimeshiftMosa_0' ~/Downloads/cc-baseline

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi/testdimeshiftLLM_0' ~/Downloads/dimeshift

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi-safe/testdimeshiftLLM_0' ~/Downloads/dimeshift-cc-safe

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/mosa/testretroboardMosa_0' ~/Downloads/cc-baseline

scp -r -P 2226 'vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi-long/testretroboardLLM_0' ~/Downloads/


scp -r -P 2226 ~/Documents/Code/cc-cut-new/dimeshift/plot_discovery_curve.py vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/Desktop/

scp -r -P 2226 ~/Documents/Code/cc-cut-new/dimeshift/codecoverage/calculate-auc.sh vagrant-cc-enhanced@localhost:/home/vagrant-cc-enhanced/workspace/codecoverage

git push fork code-coverage


 

 
