
This document outlines the necessary changes to `run.sh` and `run-test-suite.sh` to accommodate the shift from direct `docker run` commands to `docker compose up` as managed by your modified `run-docker.sh` and `docker-compose.yaml`.

---

## **1. `run.sh` Changes**

This file requires several modifications to manage Docker Compose services instead of individual containers and to correctly handle the new Chromedriver setup.

### **A. Modify `inputsValidation` Function**

Replace the existing Docker container existence check with a check for active Docker Compose services to ensure no conflicts.

```diff
--- a/run.sh
+++ b/run.sh
@@ -53,17 +53,16 @@
 	local chromedriver_port=$7
 	local express_server_directory=$8
 	local express_server_port=$9
 
-	# check if container exists
-	current_container_names=$(docker ps --format "{{.Names}}")
-	found='false'
-	for current_container_name in $current_container_names; do
-	if [[ $current_container_name == $container_name ]]; then
-		found='true'
-		fi
-	done
-	if [[ $found == 'true' ]]; then
-		echo Container with name $container_name already exists. Remove it and re-run the script.
+	# Check if docker compose services for this project might already be running
+	local pwd=$(pwd)
+	cd "$project_folder" || exit 1 # Temporarily move to project folder
+	local compose_status=$(docker compose ps -q 2>/dev/null) # Check if any services are running
+	cd "$pwd" # Return to original directory
+	if [[ ! -z "$compose_status" ]]; then
+		echo "Docker Compose services in $project_folder appear to be already running."
+		echo "Please stop them manually with 'docker compose down' before re-running the script."
 		exit 1
 	fi
 
````

### **B. Remove `stopContainer` and `killChromedriver` Functions**

These functions are no longer needed as Docker Compose will manage the stopping and removal of the application and Chromedriver services. Delete their entire definitions from `run.sh`.
```Diff

--- a/run.sh
+++ b/run.sh
@@ -95,29 +95,6 @@
 	docker rm $container_name
 }
 
-function killChromedriver(){
-	local chromedriver_port=$1
-
-	echo Stopping chromedriver process listening on port $chromedriver_port
-	local pid_chromedriver_to_kill=$(lsof -Pan -i | grep "chromedri" | grep "127.0.0.1:"$chromedriver_port | awk '{print $2}')
-	if [ -z "$pid_chromedriver_to_kill" ]; then
-	  echo Error in killing chromedriver process. PID of chromedriver is empty: $pid_chromedriver_to_kill
-	  exit 1
-	fi
-
-	echo Finding children processes of chromedriver and killing them
-	pgrep -P $pid_chromedriver_to_kill | xargs kill -9
-	kill -9 $pid_chromedriver_to_kill
-
-}
-
-
-function killExpressServer(){
-	local express_server_port=$1
-	echo Stopping express server listening on port $express_server_port
-	local pid_express_server_to_kill=$(lsof -Pan -i | grep "node" | grep "*:"$express_server_port | awk '{print $2}')
-	if [ -z "$pid_express_server_to_kill" ]; then
-	  echo Error in killing express process. PID of express server is empty: $pid_express_server_to_kill
-	  exit 1
-	fi
-
-	echo Finding children processes of express and killing them
-	pgrep -P $pid_express_server_to_kill | xargs kill -9
-	kill -9 $pid_express_server_to_kill
-}
-
 # ... (killExpressServer function remains, but modified for safety below) ...
```

### **C. Modify `killExpressServer` for Safer Exit**

Update the `killExpressServer` function to avoid exiting the script if the process is already gone, making it more robust.


```Diff
--- a/run.sh
+++ b/run.sh
@@ -118,7 +95,9 @@
 	echo Stopping express server listening on port $express_server_port
 	local pid_express_server_to_kill=$(lsof -Pan -i | grep "node" | grep "*:"$express_server_port | awk '{print $2}')
 	if [ -z "$pid_express_server_to_kill" ]; then
-	  echo Error in killing express process. PID of express server is empty: $pid_express_server_to_kill
-	  exit 1
+	  echo "Warning: PID of express server is empty. It might not be running or already cleaned up."
+	else
+		echo "Finding children processes of express and killing them"
+		pgrep -P "$pid_express_server_to_kill" | xargs kill -9
+		kill -9 "$pid_express_server_to_kill"
 	fi
-
-	echo Finding children processes of express and killing them
-	pgrep -P $pid_express_server_to_kill | xargs kill -9
-	kill -9 $pid_express_server_to_kill
 }
```

### **D. Add `stopCompose` Function**

This new function will handle the `docker compose down` command.

```Diff
--- a/run.sh
+++ b/run.sh
@@ -134,10 +111,21 @@
 	fi
 }
 
-function cleanUp(){
+# Add this new function to handle Docker Compose shutdown
+function stopCompose(){
+	local project_folder=$1 # The path where docker-compose.yaml resides
+	echo "Stopping Docker Compose services in $project_folder..."
+	local pwd=$(pwd)
+cd "$project_folder" || { echo "Error: Cannot change to $project_folder to stop compose."; exit 1; }
+	docker compose down --remove-orphans # Stop and remove services, and any unmanaged containers
+	local exit_code=$?
+	cd "$pwd" # Return to original directory
+	return $exit_code # Return the exit code of docker compose down
+}
+
+# --- MODIFIED cleanUp function ---
+function cleanUp(){
 	local container_name=$1
 	local chromedriver_port=$2
 	local express_server_port=$3
-	stopContainer $container_name
-	killChromedriver $chromedriver_port
+	stopCompose "$project_folder" "$container_name" # Pass project_folder, container_name as label
 	killExpressServer $express_server_port
 }
```

### **E. Modify `runContainer` Function**

Update this function to use `docker compose up -d` to start the services defined in your `docker-compose.yaml`.

```Diff
--- a/run.sh
+++ b/run.sh
@@ -147,21 +135,16 @@
     local project_port_app=$2
     local project_port_db=$3
     local container_name=$4
-    if [[ -e $project_folder ]]; then
-        if [[ -d $project_folder ]]; then
-            if [[ -f $project_folder/run-docker.sh ]]; then
-                local pwd=$(pwd)
-                cd $project_folder
-                if [[ $project_with_db == "true" ]]; then
-                    ./run-docker.sh -a $project_port_app -d $project_port_db -p yes -n $container_name -z yes
-                else
-                    ./run-docker.sh -a $project_port_app -p yes -n $container_name -z yes
-                fi
-                cd $pwd
-            else
-                echo $project_folder/run-docker.sh does not exist
+    if [[ -d "$project_folder" ]]; then
+        if [[ -f "$project_folder/docker-compose.yaml" ]]; then
+            local pwd=$(pwd)
+            cd "$project_folder" || { echo "Error: Cannot change to $project_folder to run compose."; exit 1; }
+
+            echo "Starting Docker Compose services for $project_name in detached mode..."
+            docker compose up -d
+            echo "Docker Compose services started."
+            cd "$pwd"
+        else
+            echo "Error: $project_folder/docker-compose.yaml does not exist. Cannot start Docker Compose."
              exit 1
          fi
-        else
-            echo $project_folder is not a directory
-            exit 1
-        fi
-    else
-        echo $project_folder path does not exists
-        exit 1
      fi
  }
  
```

### **F. Modify `scanTestSuitesFolder` Function**

Update the Chromedriver port value passed to `run-test-suite.sh` from the old `$chromedriver_port` variable to the new fixed value `4444`, as defined in your `docker-compose.yaml`.

```Diff
--- a/run.sh
+++ b/run.sh
@@ -166,7 +149,7 @@
 		echo "* Current directory: " $dir
 		cp $dir/main/* $project_folder/src/main/java/main/
 		local start_time=$(date +%s)
-		./run-test-suite.sh $project_name $session_file_name $project_port_db $project_port_app \
-			$chromedriver_port $test_suites_folder/$i $project_folder $production $express_server_port $test_suite_counter
+		./run-test-suite.sh "$project_name" "$session_file_name" "$project_port_db" "$project_port_app" \
+			"4444" "$test_suites_folder/$i" "$project_folder" "$production" "$express_server_port" "$test_suite_counter"
 		local end_time=$(date +%s)
 		local total_time_in_seconds=$(($end_time - $start_time))
 		echo "Total time to run code coverage for test suite: \
```

### **G. Modify `runCodeCoverage` Function**

Remove the manual Chromedriver startup and adjust the `cleanUp` call.

```Diff
--- a/run.sh
+++ b/run.sh
@@ -188,24 +171,21 @@
 	inputsValidation $container_name $test_suites_folder $project_folder $project_name $project_port_app \
 		$project_port_db $chromedriver_port $express_server_directory $express_server_port
 
-    runContainer $project_folder $project_port_app $project_port_db $container_name
-	echo The script starts the chromedriver process on port $chromedriver_port and \
+    runContainer "$project_folder" "$project_port_app" "$project_port_db" "$container_name"
+	echo "The script now starts Docker Compose services (webapp and chrome) and the express server for collecting coverage reports."
+	echo "Waiting for application server to start..."
+	# Keep sleep for now, but consider more robust health check waiting with docker compose commands
+	sleep 120
+
+	# REMOVED: Starting chromedriver as it's handled by Docker Compose
+	# echo "Starting chromedriver on port "$chromedriver_port
+	# chromedriver --port=$chromedriver_port &
+
+	# Start express server:
+	local current_directory=$(pwd)
+	echo "Starting express server on port "$express_server_port
 		 the express server for collecting coverage reports on port $express_server_port
-	echo Waiting for application server to start...
-	sleep 120
-
-	# Start chromedriver: chromedriver bin must be in the path
-	echo "Starting chromedriver on port "$chromedriver_port
-	chromedriver --port=$chromedriver_port &
-
-	# Start express server:
-	local current_directory=$(pwd)
-	echo "Starting express server on port "$express_server_port
 	cd $express_server_directory
 	node . $express_server_port &
 	cd $current_directory
 
 	local production="true"
 	local session_file_name=$container_name
-	scanTestSuitesFolder $test_suites_folder $project_folder $project_name \
-		$project_port_app $project_port_db $production $session_file_name \
-		$express_server_port
-
-	cleanUp $container_name $chromedriver_port $express_server_port
+	# chromedriver_port is internally updated to 4444 by runCodeCoverage before this call
+	scanTestSuitesFolder "$test_suites_folder" "$project_folder" "$project_name" \
+		"$project_port_app" "$project_port_db" "$production" "$session_file_name" \
+		"$express_server_port" # Chromedriver port is now hardcoded as 4444 in the scanTestSuitesFolder call below
+
+	cleanUp "$project_folder" "$container_name" "$express_server_port" # Pass project_folder as first arg
```

### **H. Remove Final `cleanUp` Call in `run.sh`'s Main Scope**

The `cleanUp` function is now called from within `runCodeCoverage`, so the redundant call at the very end of `run.sh` should be removed.

```Diff
--- a/run.sh
+++ b/run.sh
@@ -216,5 +216,4 @@
 total_time_in_seconds=$(($end_time - $start_time))
 echo "Total time to run code coverage for all test suites: \
 $(($total_time_in_seconds / 3600)) hours, $(($total_time_in_seconds / 60 % 60)) minutes and $(($total_time_in_seconds % 60)) seconds elapsed."
-
-# The global `project_folder` is needed for cleanUp
-cleanUp "$project_folder" "$container_name" "$chromedriver_port" "$express_server_port"
+# REMOVED: No need for this if cleanUp is called from runCodeCoverage already.
```

---

## **2. `run-test-suite.sh` Changes**

No direct code modifications are required in the `run-test-suite.sh` script itself based on the provided snippet. The script already uses the `$chromedriver_port` variable (which is `$5`) to populate `app.properties`.

The change is in the **value** that `run.sh` now passes for `chromedriver_port`. It will consistently pass `4444` (the Selenium Grid port from your `docker-compose.yaml`) to `run-test-suite.sh`. This ensures `app.properties` is configured correctly for your Dockerized Chromedriver.

---
