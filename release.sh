#!/bin/bash
./gradlew assembleRelease --console=plain
version=$(./gradlew -q printVersionName --console=plain | tail -1)
mkdir -p bin/
rm bin/*
cp app/build/outputs/apk/__32bit/release/app-__32bit-release.apk bin/browser-$version-release-unsigned__arm.apk
cp app/build/outputs/apk/__64bit/release/app-__64bit-release.apk bin/browser-$version-release-unsigned__arm64.apk

# sign 32-bit
echo "Signing 32-bit APK..."
jarsigner -verbose -sigalg SHA1withRSA \
    -digestalg SHA1 \
    -keystore lbry-android.keystore \
    -storepass $KEYSTORE_PASSWORD \
    bin/browser-$version-release-unsigned__arm.apk lbry-android > /dev/null \
    && mv bin/browser-$version-release-unsigned__arm.apk bin/browser-$version-release-signed__arm.apk
zipalign -v 4 \
    bin/browser-$version-release-signed__arm.apk bin/browser-$version-release__arm.apk > /dev/null \
    && rm bin/browser-$version-release-signed__arm.apk
echo "32-bit APK successfully built."

# sign 64-bit
echo "Signing 64-bit APK..."
jarsigner -verbose -sigalg SHA1withRSA \
    -digestalg SHA1 \
    -keystore lbry-android.keystore \
    -storepass $KEYSTORE_PASSWORD \
    bin/browser-$version-release-unsigned__arm64.apk lbry-android > /dev/null \
    && mv bin/browser-$version-release-unsigned__arm64.apk bin/browser-$version-release-signed__arm64.apk
zipalign -v 4 \
    bin/browser-$version-release-signed__arm64.apk bin/browser-$version-release__arm64.apk > /dev/null \
    && rm bin/browser-$version-release-signed__arm64.apk
echo "64-bit APK successfully built."
