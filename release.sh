#!/bin/sh
cd app
react-native bundle --platform android --dev false --entry-file src/index.js --bundle-output ../src/main/assets/index.android.bundle --assets-dest ../src/main/res/
cd ..
version=$(cat src/main/python/main.py | grep --color=never -oP '([0-9]+\.?)+')
buildozer android release
jarsigner -verbose -sigalg SHA1withRSA \
    -digestalg SHA1 \
    -keystore lbry-android.keystore \
    -storepass $KEYSTORE_PASSWORD \
    bin/browser-$version-release-unsigned.apk lbry-android \
    && mv bin/browser-$version-release-unsigned.apk bin/browser-$version-release-signed.apk
~/Dev/SDKs/android/build-tools/26.0.2/zipalign -v 4 \
    bin/browser-$version-release-signed.apk bin/browser-$version-release.apk \
    && rm bin/browser-$version-release-signed.apk


