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
## Results Collection

### From Development VMs
```bash
# Primary VM results
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/gi-wip

# Legacy VM results
scp -r -P 2224 'vagrant-old@localhost:/home/vagrant-old/Desktop/*' ~/Downloads/baseline-rerun

# Trial-specific results
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/dimeshift/initial
scp -r -P 2222 'vagrant@localhost:/home/vagrant/Desktop/*' ~/Downloads/combine-trials/retroboard/initial
```

## VM Information
- **Port 2222**: Primary development VM (vagrant@localhost)
- **Port 2224**: Baseline development VM (vagrant-old@localhost)

