# VMs Commands (Ports 2222 & 2224)

## Initial Setup

### Start Ollama Server
```bash
OLLAMA_HOST=0.0.0.0 ollama serve
```

## SSH Connections
```bash
# Primary development VM
ssh -p 2222 vagrant@localhost

# Legacy development VM
ssh -p 2224 vagrant-old@localhost
```

The password for both the VMs are: `vagrant`.

## Running the experiment

Follow the commands below to run the experiment on the benchmark application of your choice.
***IMPORTANT: These instructions are only for running one iteration of the experiment, to run the experiment for extended duration and cycles please refer to the `msc-sh-scripts` folder where it contains the shell scripts that automates the experiment.***

Navigate to the benchmark application directory (starting from the root directory)
```bash
cd workspace/fse2019/{APPLICATION_NAME}
```
***Please substitue the {APPLICATION_NAME} with either `dimeshift` or `retroboard`***

Compile the benchmark application
```bash
mvn clean compile
```

Run the experiment
```bash
./runExp.sh 1 DIGSI APOGEN 180
```
The last argument `180` represent seconds and is the search budget allocated.

## VM Information
- **Port 2222**: Primary development VM (vagrant@localhost)
- **Port 2224**: Baseline development VM (vagrant-old@localhost)

