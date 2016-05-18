# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.hostname = "databasesandlife-java-common"
  config.vm.box = "ubuntu/trusty64"
  config.vm.synced_folder ".", "/vagrant"
  
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "500"
  end
  
  # runs as root within the VM
  config.vm.provision "shell", inline: %q{
  
    set -e  # stop on error

    echo --- General OS and Java installation
    apt-get update
    DEBIAN_FRONTEND=noninteractive apt-get upgrade -q -y    # grub upgrade warnings mess with the terminal
    apt-get -q -y install vim ant subversion ntp unattended-upgrades 

    echo --- Install Java 8
    echo 'Acquire::http::Proxy { download.oracle.com DIRECT; };' >> /etc/apt/apt.conf.d/01proxy
    echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/webupd8team-java.list
    echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" >> /etc/apt/sources.list.d/webupd8team-java.list
    apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
    apt-get update
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
    apt-get -qy install oracle-java8-installer

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
  }
  
  config.vm.provision "shell", run: "always", inline: %q{
  
    set -e  # stop on error
    
    echo ''
    echo '-----------------------------------------------------------------'
    echo 'After "vagrant ssh", use:'
    echo '  ant -f /vagrant/build.xml create-jar'
    echo '  ant -f /vagrant/build.xml run-junits'
    echo '-----------------------------------------------------------------'
    echo ''
  }
  
end
