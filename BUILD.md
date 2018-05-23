## Linux Build Instructions

This app has currently only been built on Ubuntu 14.04, 16.04, 17.10, and 18.04, but these instructions, or an analog of them, should work on most Linux or macOS environments.

### Install Prerequisites

#### Requirements
* JDK 1.8
* Android SDK
* Android NDK
* Buildozer
* Node.js
* npm

#### Apt Packages
Based on the quickstart instructions at http://buildozer.readthedocs.io/en/latest/installation.html
```
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install build-essential ccache git libncurses5:i386 libstdc++6:i386 libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev openjdk-8-jdk unzip zlib1g-dev zlib1g:i386 m4 libc6-dev-i386
```
Alternatively, the JDK available from http://www.oracle.com/technetwork/java/javase/downloads/index.html can be installed instead of the `openjdk-8-jdk` package.

#### Install Cython
```
sudo pip install --upgrade cython==0.25.2
```

#### Install buildozer
A forked version of `buildozer` needs to be installed in order to copy the React Native UI source files into the corresponding directories.
```
git clone https://github.com/lbryio/buildozer.git
cd buildozer
sudo python2.7 setup.py install
```

#### Create buildozer.spec
Assuming `lbry-android` as the current working folder:
* Copy `buildozer.spec.sample` to `buildozer.spec` in the `lbry-android` folder. Running `buildozer init` instead will create a new `buildozer.spec` file.
* Update `buildozer.spec` settings to match your environment. The basic recommended settings are outlined below.


| Setting             | Description                  |
|:------------------- |:-----------------------------|
| title               | application title            |
| package.name        | package name (eg. browser)   |
| package.domain      | package domain (eg. io.lbry) |
| source.dir          | the location of the application main.py |
| version             | application version          |
| requirements        | the Python module requirements for building the application |
| services            | list of Android background services and their corresponding Python entry points |
| android.permissions | Android manifest permissions required by the application. This should be set to `INTERNET` at the very least to enable internet connectivity |
| android.api         | Android API version (Should be at least 23 for Gradle build support) |
| android.sdk         | Android SDK version (Should be at least 23 for Gradle build support) |
| android.ndk         | Android NDK version (13b has been tested to result in a successful build) |
| android.ndk_path    | Android NDK path. buildozer will automatically download this if not set |
| android.sdk_path    | Android SDK path. buildozer will automatically download this if not set |
| p4a.source_dir      | Path to the python-for-android repository folder. Currently set to the included `p4a` folder |
| p4a.local_recipes   | Path to a folder containing python_for_android recipes to be used in the build. The included `recipes` folder includes recipes for a successful build |

#### Build and Deploy
Run `npm i` in the `lbry-android/app` folder to install the necessary modules required by the React Native user interface.

Run `./build.sh` in `lbry-android` to build the APK. The output can be found in the `bin` subdirectory.

To build and deploy, you can run `./deploy.sh`. This requires a connected device or running Android emulator.

#### Development
If you already installed `Android SDK` and `adb`

* Run `adb reverse tcp:8081 tcp:8081`
* Then go to the `lbry-android/app` folder and run `npm start` 

Note: You need to have your device connected with USB debugging.

Once the bundler is ready, run the LBRY Browser app on your device and then shake the device violently until you see the React Native dev menu. You can enable "Live Reloading" and "Hot Reloading" from this menu, so any changes you make to the React Native code will be visible as you save. This will only reload React Native Javascript files. Native Java code needs to be redeployed by running the command `./deploy.sh`
