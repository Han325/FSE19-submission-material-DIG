# Code Coverage VMs Commands (Ports 2225 & 2226)

## SSH Connections
```bash
Baseline code coverage VM
ssh -p 2225 vagrant-code-coverage@localhost

Enhanced code coverage VM
ssh -p 2226 vagrant-cc-enhanced@localhost
```

## Code coverage Execution Commands

### Prerequisites
Please move the test suites that are generated from evosuite which is located in `main` folder into the benchmark application folder of your choosing (either 'Dimeshift' or 'Retroboard'), and modify the {APPLICATION_NAME} below with the corresponding application name, and set up your result directory in this structure:

```
.
└── results{APPLICATION_NAME}/
    └── {YOUR_TEST_RESULTS_NAME}/
        └── test{YOUR_TEST_RESULTS_NAME}/
            └── main/
                └── your evosuite generated test case java files

```
***Note: Set up this directory in the VM directory not in this repository.***
***P.S.: Wonder why the folder structure is so complicated? Don't ask me, ask the author, man's just following orders.***"

Please refer to the `workspace/test-generation-results` for guidance on how to setup your folders.

Once you are done setting up the directory structure, navigate to the `codecoverage` directory and swap the parameters with your values and run the command the following command below is an example for dimeshift:

```
cd workspace/codecoverage
```

```bash
./run.sh dimeshift \
         "/home/vagrant-code-coverage/workspace/test-generation-results/results{APPLICATION_NAME}/{YOUR_TEST_RESULTS}" \
         "/home/vagrant-code-coverage/workspace/fse2019/dimeshift" \
         dimeshift \
         3000 \
         3306 \
         4444 \
         "/home/vagrant-code-coverage/workspace/code-coverage-server/express-istanbul" \
         7014
```

