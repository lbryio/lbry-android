### Introduction
If you would like to contribute to the Android app, but find the build documentation a little daunting, this guide lets you copy-paste your way to a successful APK build.

#### Estimated build time
25 - 40 minutes (depending on Internet connection speeds)

#### What do you need?
* A computer running Ubuntu 18.04
* Internet access to download modules and packages.
* At least 15GB of free disk space.
* Alternatively, Docker. You can skip steps 1 through 5 if you make use of the `lbry/android-base` Docker base image. Scroll down to Fast track if you would prefer to use Docker.

### Step 1 of 10
Install all the apt packages required by running the following commands. You can copy-paste directly to your terminal.
```
sudo dpkg --add-architecture i386
sudo apt-get -y update
sudo apt-get install -y curl ca-certificates software-properties-common gpg-agent wget
sudo add-apt-repository ppa:deadsnakes/ppa -y && \
     curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list
sudo apt-get -y update && apt-get -y install autoconf autogen automake libtool libffi-dev \
     build-essential python3.7 python3.7-dev python3.7-venv python3-pip ccache git libncurses5:i386 libstdc++6:i386 \
     libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev \
     python-pip openjdk-8-jdk unzip zlib1g-dev zlib1g:i386 m4 libc6-dev-i386 yarn gawk nodejs npm
```

### Step 2 of 10
Install a couple of packages using the Python package installer
```
sudo -H pip install --upgrade cython==0.28.1 setuptools
```

### Step 3 of 10
Install buildozer, a tool for creating the apk package using the python for android toolcahin.
```
git clone https://github.com/lbryio/buildozer.git
cd buildozer && python2.7 setup.py install && cd ..
```

### Step 4 of 10
The Android SDK needs to be setup for buildozer. This requires creating a few directories and downloading a number of files. Run the following commands to create the buildozer directory, download the required archives and extract them into their proper destination folders.

```
mkdir -p ~/.buildozer/android/platform
wget 'https://dl.google.com/android/android-sdk_r23-linux.tgz' -P ~/.buildozer/android/platform/ && \
  wget 'https://dl.google.com/android/repository/platform-28_r06.zip' -P ~/.buildozer/android/platform/ && \
  wget 'https://dl.google.com/android/repository/build-tools_r26.0.2-linux.zip' -P ~/.buildozer/android/platform/
tar -xvf ~/.buildozer/android/platform/android-sdk_r23-linux.tgz -C ~/.buildozer/android/platform/ && \
  mv ~/.buildozer/android/platform/android-sdk-linux ~/.buildozer/android/platform/android-sdk-23 && \
  unzip ~/.buildozer/android/platform/platform-28_r06.zip -d ~/.buildozer/android/platform/android-sdk-23/platforms && \
  mv ~/.buildozer/android/platform/android-sdk-23/platforms/android-9 ~/.buildozer/android/platform/android-sdk-23/platforms/android-28 && \
  mkdir -p ~/.buildozer/android/platform/android-sdk-23/build-tools && \
  unzip ~/.buildozer/android/platform/build-tools_r26.0.2-linux.zip -d ~/.buildozer/android/platform/android-sdk-23/build-tools && \
  mkdir -p ~/.buildozer/android/platform/android-sdk-23/licenses && \
  echo $'\nd56f5187479451eabf01fb78af6dfcb131a6481e' > ~/.buildozer/android/platform/android-sdk-23/licenses/android-sdk-license
```

### Step 5 of 10
Install the react-native-cli npm package.
```
sudo npm install -g react-native-cli
```

### Step 6 of 10
Install the Crystax NDK which is required for building Python 3.7 for the mobile app, and a number of native C / C++ modules and packages used by the app. Run the following commands to download and extract the NDK.
```
wget 'https://www.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz' -P ~/.buildozer/android/ && \
  tar -xvf ~/.buildozer/android/crystax-ndk-10.3.2-linux-x86_64.tar.xz -C ~/.buildozer/android/ && \
  rm -rf ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9 && \
  ln -s ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21 ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9
```

### Step 7 of 10
Clone the lbryio/lbry-android git repository and create your buildozer.spec file. The provide buildozer.spec.sample contains defaults provided you followed steps 1 through 5 exactly as described. You can also customise the spec file if you want to.
```
git clone https://github.com/lbryio/lbry-android
cd lbry-android
cp buildozer.spec.sample buildozer.spec
```

### Step 8 of 10
Install the npm packages required for the app's React Native code.
```
cd app
npm install
cd ..
```

### Step 9 of 10
Copy a couple of required files from the repository for the build to be successful.
```
cp scripts/build-target-python.sh ~/.buildozer/android/crystax-ndk-10.3.2/build/tools/build-target-python.sh
cp scripts/mangled-glibc-syscalls.h ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/mangled-glibc-syscalls.h
```

### Step 10 of 10
If you made it this far, you're finally ready to build the package! You just have to run a single command to generate the APK.
```
buildozer android debug
```

### Fast Track
Install Docker and start a container using the `lbry/android-base` image, which is about 1.72GB in size. Run the following commands for Ubuntu and then follow steps 6 through 10 in the container's bash prompt.
```
sudo apt-get install docker-ce
docker run -it lbry/android-base:latest /bin/bash
```

**Protip:** You can also make use of Docker to run your builds on macOS or Windows.
