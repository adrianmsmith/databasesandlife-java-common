# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.hostname = "databasesandlife-java-common"
  config.vm.box = "ubuntu/xenial64"
  config.vm.network "forwarded_port", guest: 9999, host: 3262   # Java debugging

  if not Vagrant::Util::Platform.windows? then
    config.vm.synced_folder "~/.m2", "/home/vagrant/.m2"
    config.vm.synced_folder "~/.m2", "/root/.m2"
    config.vm.synced_folder "~/.gnupg", "/home/vagrant/.gnupg"
  end

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2000"
  end
  
  # runs as root within the VM
  config.vm.provision "shell", inline: %q{
  
    set -e  # stop on error

    echo --- General OS installation
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get upgrade -q -y    # grub upgrade warnings mess with the terminal
    apt-get -q -y install vim subversion ntp unattended-upgrades

    echo --- Install Java 8 \(OpenJDK\) and Maven
    apt-get -qy install openjdk-8-jdk maven
    echo 'export MAVEN_OPTS="-ea -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9999"' >> /etc/environment

    echo --- MySQL
    echo "mysql-server-5.5 mysql-server/root_password password root" | sudo debconf-set-selections
    echo "mysql-server-5.5 mysql-server/root_password_again password root" | sudo debconf-set-selections
    apt-get -qy install mysql-client mysql-server
    mysql -uroot -proot -e 'create database databasesandlife_common'

    echo --- PostgreSQL
    apt-get -qy install postgresql postgresql-contrib
    (cd /tmp && sudo -u postgres psql -c "alter user postgres password 'postgres'" postgres)  # os user postgres cannot see /root dir
    (cd /tmp && sudo -u postgres psql -c "create database databasesandlife_common" postgres)  
    # connect with: psql -hlocalhost databasesandlife_common postgres   (password postgres)

#   echo --- Build the software and download all dependencies
#   mvn -f /vagrant/pom.xml clean package    #this crashes Vagrant, don't know why
    echo 'mvn -f /vagrant/pom.xml package' >> ~vagrant/.bash_history
  }
  
  config.vm.provision "shell", run: "always", inline: %q{
  
    set -e  # stop on error
    
    echo ''
    echo '-----------------------------------------------------------------'
    echo 'After "vagrant ssh", use:'
    echo '  mvn -f /vagrant/pom.xml package  --> generates: target/*.jar '
    echo '  mvn -f /vagrant/pom.xml site     --> generates: target/site/apidocs '
    echo '-----------------------------------------------------------------'
    echo ''
  }
  
end
