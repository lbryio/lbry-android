# LBRY Android
[![pipeline status](https://ci.lbry.tech/lbry/lbry-android/badges/master/pipeline.svg)](https://ci.lbry.tech/lbry/lbry-android/commits/master)

An Android browser and wallet for the [LBRY](https://lbry.com) network. This app bundles [lbrynet-daemon](https://github.com/lbryio/lbry) as a background service with a UI layer built with React Native. The APK is built using buildozer and the Gradle build tool.

<img src="https://spee.ch/8/lbry-android.png" alt="LBRY Android Screenshot" width="384px" />

## Installation
The minimum supported Android version is 5.0 Lollipop. There are two ways to install:

1. Via the Google Play Store. Anyone can join the [open beta](https://play.google.com/apps/testing/io.lbry.browser) in order to install the app from the Play Store.
1. Direct APK install available at [http://build.lbry.io/android/latest.apk](http://build.lbry.io/android/latest.apk). You will need to enable installation from third-party sources on your device in order to install from this source.

## Usage
The app can be launched by opening **LBRY Browser** from the device's app drawer or via the shortcut on the home screen if that was created upon installation.

## Running from Source
The app is built from source via [Buildozer](https://github.com/kivy/buildozer). After cloning the repository, copy `buildozer.spec.sample` to `buildozer.spec` and modify this file as necessary for your environment. Please see [BUILD.md](BUILD.md) for detailed build instructions.

## Contributing
Contributions to this project are welcome, encouraged, and compensated. For more details, see https://lbry.io/faq/contributing

## License
This project is MIT licensed. For the full license, see [LICENSE](LICENSE).

## Security
We take security seriously. Please contact security@lbry.com regarding any security issues. Our PGP key is [here](https://keybase.io/lbry/key.asc) if you need it.

## Contact
The primary contact for this project is [@akinwale](https://github.com/akinwale) (akinwale@lbry.com)
