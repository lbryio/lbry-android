## Linux Build Instructions

This app has currently only been built on Ubuntu 14.04, 16.04, 17.10, and 18.04, but these instructions, or an analog of them, should work on most Linux or macOS environments.

### Install Prerequisites

#### Requirements
* JDK 1.8
* Android SDK
* Crystax Android NDK
* Buildozer
* Node.js
* npm
* yarn

#### apt Packages
Based on the quick-start instructions at http://buildozer.readthedocs.io/en/latest/installation.html
```
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install autoconf autogen build-essential curl libtool libffi-dev python python-pip python-openssl python3.7 python3.7-dev python3-pip ccache git libncurses5:i386 libstdc++6:i386 libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev openjdk-8-jdk unzip zlib1g-dev zlib1g:i386 m4 libc6-dev-i386
```
Alternatively, the JDK available from http://www.oracle.com/technetwork/java/javase/downloads/index.html can be installed instead of the `openjdk-8-jdk` package.

#### Install Cython and Setuptools
```
sudo pip install --upgrade cython==0.28.1 setuptools
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
| package.name        | package name (e.g. browser)   |
| package.domain      | package domain (e.g. io.lbry) |
| source.dir          | the location of the application main.py |
| version             | application version          |
| requirements        | the Python module requirements for building the application |
| services            | list of Android background services and their corresponding Python entry points |
| android.permissions | Android manifest permissions required by the application. This should be set to `INTERNET` at the very least to enable internet connectivity |
| android.api         | Android API version (Should be at least 23 for Gradle build support) |
| android.sdk         | Android SDK version (Should be at least 23 for Gradle build support) |
| android.ndk         | Android NDK version (not required when using crystax Android NDK) |
| android.ndk_path    | Android NDK path. This should be set to the crystax Android NDK path) |
| android.sdk_path    | Android SDK path. This should be set to the path where the Android SDK is manually set up (if not set up in the `.buildozer` path). |
| p4a.source_dir      | Path to the python-for-android repository folder. Currently set to the included `p4a` folder |
| p4a.local_recipes   | Path to a folder containing python_for_android recipes to be used in the build. The included `recipes` folder includes recipes for a successful build |

#### Setup Android SDK for buildozer
Download the Android SDK, platform and build tools archives.
* Android API 23 SDK - https://dl.google.com/android/android-sdk_r23-linux.tgz
* Android API 27 platform - https://dl.google.com/android/repository/platform-27_r01.zip
* Android build tools 26.0.1 - https://dl.google.com/android/repository/build-tools_r26.0.1-linux.

Create the `.buildozer` path (and the `android` sub-path) in your home folder if it doesn't already exist.
`mkdir ~/.buildozer`
`mkdir ~/.buildozer/android`

Extract the API 23 SDK to the `~/.buildozer/android` path and rename the extracted folder.
```
tar -xf android-sdk_r23-linux.tgz ~/.buildozer/android/platform/
mv ~/.buildozer/android/platform/android-sdk-linux ~/.buildozer/android/platform/android-sdk-23
```

Extract the API 27 platform archive into the `android-sdk-23` folder and rename the extracted folder.
```
unzip platform-27_r01.zip -d ~/.buildozer/android/platform/android-sdk-23/platforms
mv ~/.buildozer/android/platform/android-sdk-23/platforms/android-8.1.0 ~/.buildozer/android/platform/android-sdk-23/platforms/android-27
```

Extract the build tools 26.0.1 build tools into the `android-sdk-23` folder and rename the extracted folder.
```
mkdir -p ~/.buildozer/android/platform/android-sdk-23/build-tools
unzip ~/.buildozer/android/platform/build-tools_r26.0.1-linux.zip -d ~/.buildozer/android/platform/android-sdk-23/build-tools
mv ~/.buildozer/android/platform/android-sdk-23/build-tools/android-8.0.0 ~/.buildozer/android/platform/android-sdk-23/build-tools/26.0.1
```

Finally, create the Android SDK license file. This prevents being prompted to accept the SDK license during the build process.
```
mkdir -p ~/.buildozer/android/platform/android-sdk-23/licenses
echo $'\nd56f5187479451eabf01fb78af6dfcb131a6481e' > ~/.buildozer/android/platform/android-sdk-23/licenses/android-sdk-license
```

#### Setup Crystax Android NDK for buildozer
* Download the Crystax Android NDK from https://us.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz and extract. Remember to update `android.ndk_path` in your `buildozer.spec` to the path of the extracted Crystax NDK archive.
* Copy `build-target-python.sh` from the `scripts` folder in the cloned `lbry-android` repository to the `crystax-ndk-10.3.2/build/tools/` folder.
* Delete the `android-9` folder in `crystax-ndk-10.3.2/platforms`, and create a symbolic link named `android-9` to the `android-21` folder.

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
