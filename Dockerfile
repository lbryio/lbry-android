FROM plenuspyramis/android-sdk:latest

## Dependencies to run as root:

RUN dpkg --add-architecture i386 && \
    apt-get -y update && \
    apt-get install -y \
      curl ca-certificates software-properties-common gpg-agent wget \
      python3.7 python3.7-dev python3-pip python2.7 python2.7-dev python3.7-venv \
      python-pip zlib1g-dev m4 zlib1g:i386 libc6-dev-i386 gawk nodejs npm unzip openjdk-8-jdk \
      autoconf autogen automake libtool libffi-dev build-essential \
      ccache git libncurses5:i386 libstdc++6:i386 \
      libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386 && \
    npm install -g yarn && \
    pip2 install --upgrade cython setuptools && \
    pip2 install git+https://github.com/lbryio/buildozer.git@master && \
    ln -s /src/scripts/build-docker.sh /usr/local/bin/build && \
    adduser lbry-android --gecos GECOS --shell /bin/bash --disabled-password --home /home/lbry-android && \
    mkdir /home/lbry-android/.npm-packages && \
    echo "prefix=/home/lbry-android/.npm-packages" > /home/lbry-android/.npmrc && \
    chown -R lbry-android:lbry-android /home/lbry-android && \
    mkdir /src && \
    chown lbry-android:lbry-android /src && \
    mkdir /dist && \
    chown lbry-android:lbry-android /dist

## Further setup done by lbry-android user:
USER lbry-android

RUN npm install -g react-native-cli && \
    mkdir -p ~/.buildozer/android/platform && \
    wget 'https://dl.google.com/android/android-sdk_r23-linux.tgz' -P ~/.buildozer/android/platform/ && \
    wget 'https://dl.google.com/android/repository/platform-28_r06.zip' -P ~/.buildozer/android/platform/ && \
    wget 'https://dl.google.com/android/repository/build-tools_r26.0.2-linux.zip' -P ~/.buildozer/android/platform/ && \
    tar -xvf ~/.buildozer/android/platform/android-sdk_r23-linux.tgz -C ~/.buildozer/android/platform/ && \
    mv ~/.buildozer/android/platform/android-sdk-linux ~/.buildozer/android/platform/android-sdk-23 && \
    unzip ~/.buildozer/android/platform/platform-28_r06.zip -d ~/.buildozer/android/platform/android-sdk-23/platforms && \
    mv ~/.buildozer/android/platform/android-sdk-23/platforms/android-9 ~/.buildozer/android/platform/android-sdk-23/platforms/android-28 && \
    mkdir -p ~/.buildozer/android/platform/android-sdk-23/build-tools && \
    unzip ~/.buildozer/android/platform/build-tools_r26.0.2-linux.zip -d ~/.buildozer/android/platform/android-sdk-23/build-tools && \
    wget 'https://www.crystax.net/download/crystax-ndk-10.3.2-linux-x86_64.tar.xz' -P ~/.buildozer/android/ && \
    tar -xvf ~/.buildozer/android/crystax-ndk-10.3.2-linux-x86_64.tar.xz -C ~/.buildozer/android/ && \
    rm -rf ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9 && \
    ln -s ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-21 ~/.buildozer/android/crystax-ndk-10.3.2/platforms/android-9

COPY scripts/docker-build.sh /home/lbry-android/bin/build
CMD ["/home/lbry-android/bin/build"]
