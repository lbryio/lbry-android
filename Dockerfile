FROM thyrlian/android-sdk

## Dependencies to run as root:
ENV DEBIAN_FRONTEND=noninteractive
RUN dpkg --add-architecture i386 && \
    apt-get -y update && \
    apt-get install -y \
      curl ca-certificates software-properties-common gpg-agent wget \
      python3.7 python3.7-dev python3-pip python2.7 python2.7-dev python3.7-venv \
      python-pip zlib1g-dev m4 zlib1g:i386 libc6-dev-i386 gawk nodejs npm unzip openjdk-8-jdk \
      autoconf autogen automake libtool libffi-dev build-essential \
      ccache git libncurses5:i386 libstdc++6:i386 \
      libgtk2.0-0:i386 libpangox-1.0-0:i386 libpangoxft-1.0-0:i386 libidn11:i386
RUN npm install -g npm@latest
RUN npm install -g yarn react-native-cli && \
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

COPY scripts/docker-build.sh /home/lbry-android/bin/build
COPY scripts/docker-setup.sh /home/lbry-android/bin/setup
CMD ["/home/lbry-android/bin/build"]
