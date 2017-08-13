## 64-bit Ubuntu 16.04 LTS build instructions

### Install Prerequisites

#### Requirements
* JDK 1.8
* Android SDK
* Android NDK
* Buildozer

#### Apt Packages
Based on the quickstart instructions at http://buildozer.readthedocs.io/en/latest/installation.html
```
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get install build-essential ccache git libncurses5:i386 libstdc++6:i386 libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 python2.7 python2.7-dev openjdk-8-jdk unzip zlib1g-dev zlib1g:i386
```
Alternatively, the JDK available from http://www.oracle.com/technetwork/java/javase/downloads/index.html can be installed instead of the `openjdk-8-jdk` package.

**Additional package for building libgmp**
```
sudo apt-get install m4
```

**Additional package for the pycrypto recipe**

This had to be installed due to an error regarding the `<gnu/stubs.h>` include not found.
```
sudo apt-get install libc6-dev-i386
```


#### Install Cython
```
sudo pip install --upgrade cython==0.25.2
```

#### Install buildozer
```
git clone https://github.com/kivy/buildozer.git
cd buildozer
sudo python2.7 setup.py install
```

#### Create buildozer.spec
Assuming `lbrydroid` as the current working folder:
* Copy `buildozer.spec.sample` to `buildozer.spec` in the `lbry-android` folder. Running `buildozer init` instead will create a new `buildozer.spec` file.
* Update `buildozer.spec` settings to match your environment. Basic recommended settings are outlined below.


| Setting             | Description                  |
|:------------------- |:-----------------------------|
| title               | application title            |
| package.name        | package name (eg. lbrynet)   |
| package.domain      | package domain (eg. io.lbry) |
| source.dir          | the location of the application main.py |
| version             | application version          |
| requirements        | the module requirements for building the application |
| services            | list of Android background services and their corresponding Python entry points |
| android.permissions | Android manifest permissions required by the application. This should be set to `INTERNET` at the very least to enable internet connectivity |
| android.api         | Android API version (21 and 24 have been tested to result in a successful build) |
| android.sdk         | Android SDK version (21 and 24 have been tested to result in a successful build) |
| android.ndk         | Android NDK version (13b has been tested to result in a successful build) |
| android.ndk_path    | Android NDK path. buildozer will automatically download this if not set |
| android.sdk_path    | Android SDK path. buildozer will automatically download this if not set |
| p4a.source_dir      | Path to the python-for-android repository folder. Currently set to the included `p4a` folder |
| p4a.local_recipes   | Path to a folder containing python_for_android recipes to be used in the build. The included `recipes` folder includes recipes for a successful build |

#### Build and deploy the APK
Run `buildozer android debug` to build a debug APK, or `buildozer android release` to build a release APK.

To deploy and run the APK on a connected Android device, you can run `buildozer android debug deploy run`.

Run `buildozer android clean` to clean the package if you intend to rebuild.
