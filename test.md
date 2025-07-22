./run.sh splittypie \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa" \
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

./run.sh retroboard \
         "/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/mosa" \
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

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/Desktop/*' ~/Downloads/testing-faults

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsSplittypie/mosa/testsplittypieMosa_0' ~/Downloads/modified-code-coverage

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsDimeshift/mosa/testdimeshiftMosa_0' ~/Downloads/cc-baseline

scp -r -P 2225 'vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/test-generation-results/resultsRetroboard/mosa/testretroboardMosa_0' ~/Downloads/cc-baseline


scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/plot_discovery_curve.py vagrant-code-coverage@localhost:/home/vagrant-code-coverage/Desktop/

scp -r -P 2225 ~/Documents/Code/code-coverage/workspace/codecoverage/calculate-auc.sh vagrant-code-coverage@localhost:/home/vagrant-code-coverage/workspace/codecoverage

git push fork code-coverage


 