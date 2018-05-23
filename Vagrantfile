echoed=false

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"
  
  config.vm.provider "virtualbox" do |v|
    host = RbConfig::CONFIG['host_os']
    
    # Give VM 1/4 system memory & access to all cpu cores on the host
    if host =~ /darwin/
      cpus = `sysctl -n hw.ncpu`.to_i
      # sysctl returns Bytes and we need to convert to MB
      mem = `sysctl -n hw.memsize`.to_i / 1024 / 1024 / 4
    elsif host =~ /linux/
      cpus = `nproc`.to_i
      # meminfo shows KB and we need to convert to MB
      mem = `grep 'MemTotal' /proc/meminfo | sed -e 's/MemTotal://' -e 's/ kB//'`.to_i / 1024 / 4
    else
      cpus = `wmic cpu get NumberOfCores`.split("\n")[2].to_i
      mem = `wmic OS get TotalVisibleMemorySize`.split("\n")[2].to_i / 1024 /4
    end
    
    mem = mem / 1024 / 4
    mem = [mem, 2048].max # Minimum 2048
    
	if echoed === false
	  echoed=true
      puts("Memory", mem)
      puts("CPUs", cpus)
	end
	
    v.customize ["modifyvm", :id, "--memory", mem]
    v.customize ["modifyvm", :id, "--cpus", cpus]
  end
  
  config.vm.provision "shell", inline: <<-SHELL
    dpkg --add-architecture i386
	apt-get update
	apt-get install -y build-essential ccache git libncurses5:i386 libstdc++6:i386 libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev openjdk-8-jdk unzip zlib1g-dev zlib1g:i386 m4 libc6-dev-i386 python-pip
	pip install --yes --upgrade cython==0.25.2 setuptools
	git clone https://github.com/lbryio/buildozer.git
    cd buildozer
    python2.7 setup.py install
  SHELL
end