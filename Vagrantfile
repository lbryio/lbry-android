echoed=false

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/bionic64"
  #config.disksize.size = "20GB"

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

	#v.customize ["setextradata", :id, "VBoxInternal2/SharedFoldersEnableSymlinksCreate/home_vagrant_lbry-android", "1"]
	#v.customize ["setextradata", :id, "VBoxInternal2/SharedFoldersEnableSymlinksCreate/vagrant", "1"]
    v.customize ["modifyvm", :id, "--memory", mem]
    v.customize ["modifyvm", :id, "--cpus", cpus]
  end

  config.vm.synced_folder "./", "/home/vagrant/lbry-android"


  config.vm.provision "shell", inline: <<-SHELL
    dpkg --add-architecture i386
	apt-get update
	apt-get install -y libssl-dev
	apt-get install -y python3.6 python3.6-dev python3-pip autoconf libffi-dev pkg-config libtool build-essential ccache git libncurses5:i386 libstdc++6:i386 libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev openjdk-8-jdk unzip zlib1g-dev zlib1g:i386 m4 libc6-dev-i386 python-pip
	pip install -f --upgrade setuptools pyopenssl
	git clone https://github.com/lbryio/buildozer.git
    cd buildozer
    python2.7 setup.py install
	cd ../
	rm -rf ./buildozer

	# Install additonal buildozer dependencies
	sudo apt-get install cython

	# Install node
	curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
	sudo apt-get install -y nodejs

	export HOME=/home/vagrant

	cp $HOME/lbry-android/buildozer.spec.vagrant $HOME/lbry-android/buildozer.spec

	mkdir -p cd $HOME/.buildozer/android/platform/
	wget -q 'https://us.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz' -P $HOME/.buildozer/android/
	wget -q 'https://dl.google.com/android/android-sdk_r23-linux.tgz' -P $HOME/.buildozer/android/platform/
	wget -q 'https://dl.google.com/android/repository/platform-27_r01.zip' -P $HOME/.buildozer/android/platform/
	wget -q 'https://dl.google.com/android/repository/build-tools_r26.0.1-linux.zip' -P $HOME/.buildozer/android/platform/
	tar -xf ~/.buildozer/android/crystax-ndk-10.3.2-linux-x86_64.tar.xz -C $HOME/.buildozer/android/
	rm $HOME/.buildozer/android/crystax-ndk-10.3.2-linux-x86_64.tar.xz
	ln -s $HOME/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21 $HOME/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9
	cp -f $HOME/lbry-android/scripts/build-target-python.sh $HOME/.buildozer/android/crystax-ndk-10.3.2/build/tools/build-target-python.sh
	rm -rf $HOME/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9
	tar -xf $HOME/.buildozer/android/platform/android-sdk_r23-linux.tgz -C $HOME/.buildozer/android/platform/
	rm $HOME/.buildozer/android/platform/android-sdk_r23-linux.tgz
	mv $HOME/.buildozer/android/platform/android-sdk-linux $HOME/.buildozer/android/platform/android-sdk-23
	unzip -qq $HOME/.buildozer/android/platform/android-23_r02.zip -d $HOME/.buildozer/android/platform/android-sdk-23/platforms
	rm $HOME/.buildozer/android/platform/platform-27_r01.zip
	mv $HOME/.buildozer/android/platform/android-sdk-23/platforms/android-8.1.0 $HOME/.buildozer/android/platform/android-sdk-23/platforms/android-27
	mkdir -p $HOME/.buildozer/android/platform/android-sdk-23/build-tools
	unzip -qq $HOME/.buildozer/android/platform/build-tools_r26.0.1-linux.zip -d $HOME/.buildozer/android/platform/android-sdk-23/build-tools
	rm $HOME/.buildozer/android/platform/build-tools_r26.0.1-linux.zip
	mv $HOME/.buildozer/android/platform/android-sdk-23/build-tools/android-8.0.0 $HOME/.buildozer/android/platform/android-sdk-23/build-tools/26.0.1
	mkdir -p $HOME/.buildozer/android/platform/android-sdk-23/licenses

	rm -rf $HOME/.buildozer/android/platform/android-sdk-23/tools
	#  https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip
	wget -q https://dl.google.com/android/repository/tools_r25.2.5-linux.zip
	unzip -o -q ./tools_r25.2.5-linux.zip -d $HOME/.buildozer/android/platform/android-sdk-23/
	rm sdk-tools-linux-3859397.zip

	echo $'\nd56f5187479451eabf01fb78af6dfcb131a6481e' > $HOME/.buildozer/android/platform/android-sdk-23/licenses/android-sdk-license

	sudo chown -r vagrant $HOME

	echo "Installing React Native via NPM..."
	sudo npm install -g react-native-cli
  SHELL
end
