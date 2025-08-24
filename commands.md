# Development VMs Commands (Ports 2222 & 2224)

## Initial Setup

### Start Ollama Server
```bash
OLLAMA_HOST=0.0.0.0 ollama serve
```

## SSH Connections
```bash
# Primary development VM
ssh -i ~/.ssh/id_utm_vm -p 2222 vagrant@localhost

# Legacy development VM
ssh -p 2224 vagrant-old@localhost
```

## File Transfers & Setup

### Initial Project Setup (Port 2222)
```bash
# Transfer Maven dependencies
scp -r -P 2222 ~/.m2/repository/org/evosuite/ vagrant@localhost:/home/vagrant/.m2/repository/org/

# Transfer test applications
scp -r -P 2222 ~/Documents/Code/dig/fse2019/dimeshift/ vagrant@localhost:/home/vagrant/workspace/fse2019/
scp -r -P 2222 ~/Documents/Code/dig/fse2019/retroboard/ vagrant@localhost:/home/vagrant/workspace/fse2019/
```

## Test Execution Commands

### Dimeshift Tests (with MySQL on port 3306)
```bash
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

./run.sh dimeshift \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsDimeshift/llmgi" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014
```

### Retroboard Tests
```bash
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

./run.sh retroboard \
         "/home/vagrant-cc-enhanced/workspace/test-generation-results/resultsRetroboard/llmgi" \
         "/home/vagrant-cc-enhanced/workspace/fse2019/retroboard" \
         retroboard \
         3000 \
         0 \
         4444 \
         "/home/vagrant-cc-enhanced/workspace/code-coverage-server/express-istanbul" \
         7014
```

## Results Collection

### From Development VMs
```bash
# Primary VM results
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/gi-wip

# Legacy VM results
scp -r -P 2224 'vagrant-old@localhost:/home/vagrant-old/Desktop/*' ~/Downloads/baseline-rerun

# Trial-specific results
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/dimeshift/initial
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/splittypie/initial
```

## VM Information
- **Port 2222**: Primary development VM (vagrant@localhost)
- **Port 2224**: Baseline development VM (vagrant-old@localhost)

