VM_CPUS = 10
VM_MEMORY = 14336

Vagrant.configure('2') do |config|
    $script = <<-SHELL

        #!/bin/bash

        # Install Maven (if not already installed)
        apt-get update
        apt-get install -y ca-certificates curl wget
        apt-get install -y openjdk-8-jdk
        apt-get install -y maven

        mkdir -p /home/vagrant/.m2/repository/org/seleniumhq
        mkdir -p /home/vagrant/Desktop
        chown -R vagrant:vagrant /home/vagrant/.m2
        chown -R vagrant:vagrant /home/vagrant/Desktop

        su - vagrant -c 'cp -r /home/vagrant/workspace/selenium-custom-library/selenium /home/vagrant/.m2/repository/org/seleniumhq'

        # compile apps
        for app in $(ls /home/vagrant/workspace/fse2019)
        do
            su - vagrant -c "cd /home/vagrant/workspace/fse2019/$app && mvn clean compile"
        done

        # install evosuite
        su - vagrant -c 'cd /home/vagrant/workspace/evosuite && mvn clean install -DskipTests'

        cd /home/vagrant

        # Install Docker
        install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
        chmod a+r /etc/apt/keyrings/docker.asc
        echo \
            "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
            $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
            tee /etc/apt/sources.list.d/docker.list > /dev/null
        apt-get update
        apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
        usermod -aG docker vagrant

        docker pull selenium/standalone-chrome:3.141.59-dubnium

    SHELL

    config.vm.provider 'virtualbox' do |v|
        v.memory = VM_MEMORY
        v.cpus = VM_CPUS
    end

    config.vm.box = "ubuntu/bionic64"
    config.vm.synced_folder "./", "/home/vagrant/workspace"

    config.vm.provision :shell, inline: $script, privileged: true

end

