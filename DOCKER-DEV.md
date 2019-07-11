# lbry-android development environment inside docker

[scripts/lbry-android.sh](scripts/lbry-android.sh) is a bash script to create a
docker container for lbry-android development. 

This is a hybrid approach where the apk is built inside docker, but you run
Android Studio, `adb`, and the app hot-reload bundler, directly on your host.

## Features

 * Clones lbry-android source code to a directory on the host, and mounts it
   inside the container.
 * Installs all build dependencies inside the container, leaving your host
   computer clean.
 * Mounted `.buildozer` directory to save container space. (Docker containers
   should stay under 10GB, and `.buildozer` is too big, so it has to stay in a
   mounted host directory instead.)
 * The biggest downloads are cached in `.buildozer-downloads` directory so you
   can easily remove `.buildozer` and not have to re-download the large cryostax
   NDK more than once.
 * Instructions for installing on a real Android device, and for setting up
   hot-reload.
 * Only a handful of commands to go from zero->hero.

## Requirements

Install all of the following on your host computer:

 * Tested on Linux x86_64 with BASH.
 * [Install Android Studio](https://developer.android.com/studio/).
   * The normal install auto-creates a directory in `$HOME/Android/Sdk` to store
     SDK downloads in your home directory.
 * [Install nodejs](https://nodejs.org/en/download/package-manager/).
 * [Install yarn](https://yarnpkg.com/lang/en/docs/install).
 * [Install Docker](https://docs.docker.com/install/).
 * Install `sudo` and give your user account access to run `sudo docker`. 
 
## Install

Clone `lbry-android`:

```
LBRY_GIT=$HOME/git/vendor/lbryio/
mkdir -p $LBRY_GIT
git clone https://github.com/lbryio/lbry-android.git $LBRY_GIT
cd $LBRY_GIT/lbry-android
git submodule update --init --recursive
```

Install a bash alias to the [scripts/lbry-android.sh](scripts/lbry-android.sh)
script:

```
echo "alias lbry-android=$LBRY_GIT/lbry-android/scripts/lbry-android.sh" >> $HOME/.bashrc

source ~/.bashrc
```

## Usage

 * First create the base docker image:
 
 ```
 lbry-android docker-build
 ```
 
(If anytime you change the docker build scripts in the [scripts](scripts)
subdirectory, you should rebuild the image again.)

 * Setup buildozer and install dependencies:
 
 ```
 lbry-android setup
 ```

 * Build the apk:
 
 ```
 lbry-android build
 ```

The apk will be built and end up in the `lbry-android/bin/` subdirectory.

## Running on a real Android device

Once you have the apk built, you can install it on a real Android device to
start testing.

Follow the Android documentation for [enabling USB
debugging](https://developer.android.com/studio/command-line/adb#Enabling) on
your device.

Once you have enabled debugging, do the following:

 * Plug your device into a USB port on the host computer.
 * Run: 

 ```~/Android/Sdk/platform-tools/adb devices```

 * ADB should list your device like so:
 
 ```
 [ryan@t440s lbry-android]$ adb devices
 List of devices attached
 HT71R0000000	device
 ```

 * The first time you connect, you should see a modal dialog on the device
   asking to confirm the host id. You must approve it, or adb will fail to
   connect. 
   
 * If after trying several times, adb is still not connecting to your device,
   and adb lists your device as `unauthorized`, you may need to [follow this
   advice](https://stackoverflow.com/a/38380384/56560) and delete your
   `$HOME/.android` directory.

 * Install the apk (whatever the current version is called) to the device:
 
 ```
 ~/Android/Sdk/platform-tools/adb install ./bin/browser-0.7.5-debug.apk
 ```

 * Open the app on your device, and follow the initial steps to get to the main
   lbry-android browser page.
   
 * Create a tcp tunnel over the adb bridge, for the app to handle live-reload:
 
 ```
 ~/Android/Sdk/platform-tools/adb reverse tcp:8081 tcp:8081
 ```

 * Start the live-reload server on the host:
 
 ```
 cd app/
 yarn install
 yarn start
 ```

 * With your device currently running the lbry-android app, shake the device
   from side to side, and a menu will appear. Choose `Enable Hot Reloading`.
   
 * The app should reload itself.
 
 * Now make a change to the source code to test hot reload. From the host, open
   up the main page view source code: `app/src/component/uriBar/view.js`. Find
   the line that reads `Search movies, music, and more` (This is the main search
   bar at the top of the app.) - Change some of the text and save the file.
   
 * If hot-reload is working, within a few seconds the phone should reload the
   page automatically and show the change that you made.
   
 * If the hot-reload does not work, try closing the app (dismiss the window from
   the tab overview, via android square button) and reload the app.
   

