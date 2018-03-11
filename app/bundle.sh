#!/bin/sh
react-native bundle --platform android --dev false --entry-file src/index.js --bundle-output ../src/main/assets/index.android.bundle --assets-dest ../src/main/res/
