#!/bin/bash

(
    ANDROID_STUDIO_SDK=${ANDROID_STUDIO_SDK:-$HOME/Android/Sdk}
    LBRY_ANDROID_HOME=${LBRY_ANDROID_HOME:-$HOME/git/vendor/lbryio/lbry-android}
    LBRY_ANDROID_BUILDOZER_HOME=${LBRY_ANDROID_BUILDOZER_HOME:-$LBRY_ANDROID_HOME/.buildozer}
    LBRY_ANDROID_BUILDOZER_DOWNLOADS=${LBRY_ANDROID_BUILDOZER_DOWNLOADS:-$LBRY_ANDROID_HOME/.buildozer-downloads}
    LBRY_ANDROID_REPO=${LBRY_ANDROID_REPO:-https://www.github.com/lbryio/lbry-android}
    LBRY_ANDROID_IMAGE=${LBRY_ANDROID_IMAGE:-lbry-android:local}

    ## Logger utility:
    exe() { ( echo "## $*"; $*; ) }

    ## Confirmation dialog:
    ## usage: prompt_confirm "Overwrite File?" || return 1
    prompt-confirm() {
        while true; do
            read -r -n 1 -p "${1:-Continue?} [y/n]: " REPLY
            case $REPLY in
                [yY]) echo ; return 0 ;;
                [nN]) echo ; return 1 ;;
                *) printf " \033[31m %s \n\033[0m" "invalid input"
            esac
        done
    }

    check-dependencies() {
        if [ ! -d $ANDROID_STUDIO_SDK ]; then
            echo "Error: $ANDROID_STUDIO_SDK not found."
            echo "You must download and install Android Studio:"
            echo "https://developer.android.com/studio/"
            return 1
        elif ! which docker > /dev/null; then
            echo "You must install docker and setup sudo to access it as regular user."
            return 1
        elif ! which sudo > /dev/null; then
            echo "You must install sudo and setup user account for sudo priviliges."
            return 1
        fi
    }

    check-src-dir() {
        if [ ! -d $LBRY_ANDROID_HOME ]; then
            echo "Cannot find lbry-android source code:"
            echo "$LBRY_ANDROID_HOME"
            echo ""
            echo "Clone the lbry-android repository first, run:"
            echo "  lbry-android clone"
            echo ""
            echo "To use a different path set the LBRY_ANDROID_HOME variable."
            return 1
        fi
    }

    setup() {
        set -e
        check-src-dir || return 1
        exe $HOME/Android/Sdk/tools/bin/sdkmanager "platforms;android-27"
        if [ -d $LBRY_ANDROID_BUILDOZER_HOME ]; then
            echo "Buildozer path already exists: $LBRY_ANDROID_BUILDOZER_HOME"
            echo "If you would like to re-install from scratch, delete that directory first."
        else
            mkdir -p $LBRY_ANDROID_BUILDOZER_HOME
            mkdir -p $LBRY_ANDROID_BUILDOZER_DOWNLOADS
            exe sudo docker run --rm -it \
                -v $LBRY_ANDROID_HOME:/src \
                -v $LBRY_ANDROID_BUILDOZER_HOME:/home/lbry-android/.buildozer/ \
                -v $LBRY_ANDROID_BUILDOZER_DOWNLOADS:/home/lbry-android/.buildozer-downloads/ \
                $LBRY_ANDROID_IMAGE \
                /home/lbry-android/bin/setup
        fi
    }

    ## Build lbry-android docker image
    docker-build(){
        check-src-dir || return 1
        sudo docker build -t $LBRY_ANDROID_IMAGE $LBRY_ANDROID_HOME
    }

    ## Build lbry-android apk
    ANDROID_SDK_LICENSE=$ANDROID_STUDIO_SDK/licenses/android-sdk-license
    build(){
        if [ ! -f $ANDROID_SDK_LICENSE ]; then
            echo "Android SDK license file not found:"
            echo $ANDROID_SDK_LICENSE
            echo "Open Android-Studio, download the SDK and accept the license agreement."
            return 1
        fi
        if [ ! -d $LBRY_ANDROID_BUILDOZER_HOME ]; then
            echo "Buildozer root not found: $LBRY_ANDROID_BUILDOZER_HOME"
            echo "Run: lbry-android setup"
            return 1
        fi
        check-src-dir || return 1
        mkdir -p $LBRY_ANDROID_HOME/.gradle
        exe sudo docker run --rm -it \
            -v $LBRY_ANDROID_HOME:/src \
            -v $LBRY_ANDROID_BUILDOZER_HOME:/home/lbry-android/.buildozer/ \
            -v $LBRY_ANDROID_HOME/.gradle:/home/lbry-android/.gradle/ \
            -v $ANDROID_SDK_LICENSE:/home/lbry-android/.buildozer/android/platform/android-sdk-23/licenses/android-sdk-license \
             $LBRY_ANDROID_IMAGE
    }

    clone() {
        if [ -d $LBRY_ANDROID_HOME ]; then
            echo "$LBRY_ANDROID_HOME already exists."
            echo "If you wish to use a different path set the LBRY_ANDROID_HOME variable."
            return 1
        else
            exe git clone $LBRY_ANDROID_REPO $LBRY_ANDROID_HOME
            echo ""
            echo "lbry-android clone complete."
            echo "LBRY_ANDROID_HOME=$LBRY_ANDROID_HOME"
        fi
    }

    SUBCOMMANDS_NO_ARGS=(setup clone docker-build build)
    SUBCOMMANDS_PASS_ARGS=(none)

    check-dependencies || return 1

    if printf '%s\n' ${SUBCOMMANDS_NO_ARGS[@]} | grep -q -P "^$1$"; then
        ## Subcommands that take no arguments:
        (
            set -e
            if [ "$#" -eq 1 ]; then
                $*
            else
                echo "$1 does not take any additional arguments"
            fi
        )
    elif printf '%s\n' ${SUBCOMMANDS_PASS_ARGS[@]} | grep -q -P "^$1$"; then
        ## Subcommands that pass all arguments:
        (
            set -e
            $*
        )
    else
        if [[ $# -gt 0 ]]; then
            echo "## Invalid command: $1"
        else
            echo "## Must specify a command:"
        fi
        echo ""
        echo "##   lbry-android setup"
        echo "##     - Sets up buildozer and downloads dependencies"
        echo "##   lbry-android docker-build"
        echo "##     - Builds the lbry-android docker container"
        echo "##   lbry-android build"
        echo "##     - Builds the lbry-android apk"
    fi
)

