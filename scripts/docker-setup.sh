#!/bin/bash
set -e

exe() { ( echo "## $*"; $*; ) }

BUILDOZER_HOME=$HOME/.buildozer
BUILDOZER_DOWNLOADS=$HOME/.buildozer-downloads

exe mkdir -p $BUILDOZER_HOME/android/platform
exe wget -c 'https://dl.google.com/android/android-sdk_r23-linux.tgz' -P $BUILDOZER_DOWNLOADS
exe wget -c 'https://dl.google.com/android/repository/platform-28_r06.zip' -P $BUILDOZER_DOWNLOADS
exe wget -c 'https://dl.google.com/android/repository/build-tools_r26.0.2-linux.zip' -P $BUILDOZER_DOWNLOADS
exe tar -xvf $BUILDOZER_DOWNLOADS/android-sdk_r23-linux.tgz -C $BUILDOZER_HOME/android/platform/
exe mv $BUILDOZER_HOME/android/platform/android-sdk-linux $BUILDOZER_HOME/android/platform/android-sdk-23
exe unzip $BUILDOZER_DOWNLOADS/platform-28_r06.zip -d $BUILDOZER_HOME/android/platform/android-sdk-23/platforms
exe mv $BUILDOZER_HOME/android/platform/android-sdk-23/platforms/android-9 $BUILDOZER_HOME/android/platform/android-sdk-23/platforms/android-28
exe mkdir -p $BUILDOZER_HOME/android/platform/android-sdk-23/build-tools
exe unzip $BUILDOZER_DOWNLOADS/build-tools_r26.0.2-linux.zip -d $BUILDOZER_HOME/android/platform/android-sdk-23/build-tools
exe wget -c 'https://www.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz' -P $BUILDOZER_DOWNLOADS
exe tar -xvf $BUILDOZER_DOWNLOADS/crystax-ndk-10.3.2-linux-x86_64.tar.xz -C $BUILDOZER_HOME/android/
exe rm -rf $BUILDOZER_HOME/android/crystax-ndk-10.3.2/platforms/android-9
exe ln -s $BUILDOZER_HOME/android/crystax-ndk-10.3.2/platforms/android-21 $BUILDOZER_HOME/android/crystax-ndk-10.3.2/platforms/android-9
exe mkdir -p $BUILDOZER_HOME/android/crystax-ndk-10.3.2/build/tools/
exe mkdir -p $BUILDOZER_HOME/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/
exe cp /src/scripts/build-target-python.sh ~/.buildozer/android/crystax-ndk-10.3.2/build/tools/build-target-python.sh
exe cp /src/scripts/mangled-glibc-syscalls.h ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/mangled-glibc-syscalls.h
exe mv $BUILDOZER_HOME/android/platform/android-sdk-23/build-tools/android-8.1.0 $BUILDOZER_HOME/android/platform/android-sdk-23/build-tools/26.0.2
