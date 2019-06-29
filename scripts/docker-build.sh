#!/bin/bash
set -e

exe() { ( echo "## $*"; $*; ) }

## ANDROID_SDK_LICENSE variable is required:
ANDROID_SDK_LICENSE=${ANDROID_SDK_LICENSE:-none}
## VERSION and REPO variables are optional:
## Use 'none' as a way to detect that none was provided by the user:
VERSION=${VERSION:-none}
REPO=${REPO:-none}

if [ $ANDROID_SDK_LICENSE == "none" ]; then
    echo "No Android SDK License provided."
    echo "1) Download android-studio."
    echo "2) Go to Settings -> Android SDKs -> Select Android 6.0 (Marshmellow)"
    echo "   and click the little download button and then agree to the license."
    echo "3) Run: cat \$HOME/Android/Sdk/licenses/android-sdk-license && echo"
    echo "4) Copy the hexdigits - this is your Android SDK License."
    echo "5) Rerun this build and add the extra argument (before the image name!)"
    echo ""
    echo "This is what the *end* of your build command should look like:"
    echo "    -e ANDROID_SDK_LICENSE=xxxxxxxxxxxxxxxxxxxxxxxxx lbry-android"
    echo ""
    exit 1
else
    mkdir -p $HOME/.buildozer/android/platform/android-sdk-23/licenses
    echo $ANDROID_SDK_LICENSE > $HOME/.buildozer/android/platform/android-sdk-23/licenses/android-sdk-license
fi

## Two options for where the lbry-android source code comes from:
##  1) Clone directly from git via provided VERSION and REPO environment variables.
##  2) User may mount their own lbry-android source tree at /src
## Only one of these two options can be used at a time.
## VERSION is any valid git reference: commit, branch, or tag.
## REPO is the git repository URL to clone.

## User must create their own buildozer.spec and google-services.json
## This may be done in their own fork of lbry-android,
##   or done in their own clone mounted to /src

if [ $VERSION != "none" ] || [ $REPO != "none" ]; then
    # Build from a fresh git clone

    # No /src should be mounted if VERSION or REPO specified:
    if mount | grep " /src"; then
        echo "Cannot mount /src when VERSION and/or REPO variables are used."
        echo "Aborting."
        exit 1
    fi

    # A /dist directory should exist to copy final apk to:
    if ! mount | grep " /dist"; then
        echo "When using VERSION or REPO you must mount a /dist directory to put the final apk"
        echo "Aborting."
        exit 1
    fi
    if [ $VERSION == "none" ]; then
        VERSION=master
    fi
    if [ $REPO == "none" ]; then
        REPO="https://github.com/lbryio/lbry-android.git"
    fi

    ## Clone from $REPO and checkout $VERSION:
    exe git clone $REPO /src
    cd /src
    exe git checkout $VERSION

    ## Create config from samples if none exists:
    if [ ! -f /src/buildozer.spec ]; then
        exe cp /src/buildozer.spec.sample /src/buildozer.spec
    fi
    if [ ! -f /src/p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.json ]; then
        exe cp /src/p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.sample.json /src/p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.json
    fi
fi

if [ ! -f /src/buildozer.spec ]; then
    echo "You must create a buildozer.spec file (See buildozer.spec.sample)"
    echo "Aborting."
    exit 1
elif [ ! -f /src/p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.json ]; then
    echo "You must create p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.json "
    echo "  (See p4a/pythonforandroid/bootstraps/lbry/build/templates/google-services.sample.json)"
    echo "Aborting."
    exit 1
fi

## Setup npm for non-root user:
NPM_PACKAGES="${HOME}/.npm-packages"
NODE_PATH="$NPM_PACKAGES/lib/node_modules:$NODE_PATH"
PATH="$NPM_PACKAGES/bin:$PATH"

## Build:
cd /src/app
exe npm install
exe /src/app/bundle.sh

exe mkdir -p $HOME/.buildozer/android/crystax-ndk-10.3.2/build/tools/
exe mkdir -p $HOME/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/
exe cp /src/scripts/build-target-python.sh ~/.buildozer/android/crystax-ndk-10.3.2/build/tools/build-target-python.sh
exe cp /src/scripts/mangled-glibc-syscalls.h ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21/arch-arm/usr/include/crystax/bionic/libc/include/sys/mangled-glibc-syscalls.h
exe mv $HOME/.buildozer/android/platform/android-sdk-23/build-tools/android-8.1.0 $HOME/.buildozer/android/platform/android-sdk-23/build-tools/26.0.2

cd /src
exe buildozer android debug

if mount | grep " /dist"; then
    exe cp /src/bin/* /dist
fi
