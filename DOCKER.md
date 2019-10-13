# Introduction
The purpose of this guide is to help whoever is interested in running the LBRY Android application from scratch on their device, but their main computing platform is not Linux but macOS.

## Estimated build time
25 - 40 minutes (depending on Internet connection speeds)

## What do you need?
* A computer running the latest OS
* Internet access to download modules and packages
* At least 15GB of free disk space
* Docker
* Patience

## Step 1/6
Create an application on [Firebase](https://console.firebase.google.com). In the **Android package name** field, input `io.lbry.browser`. Download the resulting `google-services.json` file and keep it safe, you'll be needing it later.

## Step 2/6
Start the docker application and paste all of these lines into Terminal:

```bash
docker run -it lbry/android-base:latest /bin/bash
wget "https://www.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz" -P ~/.buildozer/android/
tar -xvf ~/.buildozer/android/crystax-ndk-10.3.2-linux-x86_64.tar.xz -C ~/.buildozer/android/
rm -rf ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9
ln -s ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21 ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9
git clone https://github.com/lbryio/lbry-android
cd lbry-android
git submodule update --init --recursive
cp buildozer.spec.sample buildozer.spec
cd app;npm i;cd ..
cp scripts/build-target-python.sh ~/.buildozer/android/crystax-ndk-10.3.2/build/tools/build-target-python.sh
cp scripts/mangled-glibc-syscalls.h ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/mangled-glibc-syscalls.h
cd p4a/pythonforandroid/bootstraps/lbry/build/templates
apt install nano -y
```

## Step 3/6
Copy the contents of the `google-services.json` you downloaded earlier and paste them into Terminal after running the next command:

```bash
nano google-services.json
```

Type `^X` to save and exit.

## Step 4/6
Paste more lines and I guess check your email, this will take some time:

```bash
cd /lbry-android/app
./bundle.sh
cd ..
bulldozer android debug
```

When the build is complete, you should see a message like: `[INFO]:    # APK renamed to browser-0.7.3-debug.apk`. You will need this filename for the next step.

## Step 5/6
In a separate Terminal window:

```bash
docker ps # get container name
docker cp CONTAINER_NAME:/lbry-android/bin/STEP_4_FILENAME ~/Desktop/ # copies STEP_4_FILENAME to your Desktop
```

## Step 6/6
- Download [Android File Transfer](https://www.android.com/filetransfer) and install it.
- On your Android device, install [File Explorer](https://play.google.com/store/apps/details?id=com.mauriciotogneri.fileexplorer).
- Plugin your Android device and swipe down from the top into the "USB for file transfer" settings (or similar name on your device) and make sure "Transfer files" is selected.
- Open **Android File Transfer** on your computer and drag and drop `STEP_4_FILENAME` from your Desktop to the `Downloads` folder on the Android device.
- Back on the Android device, navigate to `STEP_4_FILENAME` in the `Downloads` folder and tap it to begin the installation.
