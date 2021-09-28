# LBRY Android
[![pipeline status](https://ci.lbry.tech/lbry/lbry-android/badges/master/pipeline.svg)](https://ci.lbry.tech/lbry/lbry-android/commits/master)
[![GitHub license](https://img.shields.io/github/license/lbryio/lbry-android)](https://github.com/lbryio/lbry-android/blob/master/LICENSE)

An Android browser and wallet for the [LBRY](https://lbry.com) network.


<img src="https://spee.ch/@lbry:3f/android-08-homepage.gif" alt="LBRY Android GIF" width="384px" />


## Installation
The minimum supported Android version is 5.0 Lollipop. There are two ways to install:

1. Via the Google Play Store. Anyone can join the [open beta](https://play.google.com/apps/testing/io.lbry.browser) in order to install the app from the Play Store.
1. Direct APK install available at [http://build.lbry.io/android/latest.apk](http://build.lbry.io/android/latest.apk). You will need to enable installation from third-party sources on your device in order to install from this source.

## Usage
The app can be launched by opening **LBRY** from the device's app drawer or via the shortcut on the home screen if that was created upon installation.

## Running from Source
Clone the repository and open the project in Android Studio. Android Studio will automatically run the initial build process.

Create file 'twitter.properties' in app/ folder with the following content:

```
twitterConsumerKey=XXXXXX

twitterConsumerSecret=XXXXXX
```

Click the Sync button and when process finishes, the Run button to launch the app on your simulator or connected debugging device after the build process is complete.

## Contributing
Contributions to this project are welcome, encouraged, and compensated. For more details, see https://lbry.io/faq/contributing

## License
This project is MIT licensed. For the full license, see [LICENSE](LICENSE).

## Security
We take security seriously. Please contact security@lbry.com regarding any security issues. Our PGP key is [here](https://lbry.com/faq/pgp-key) if you need it.

## Contact
The primary contact for this project is [@akinwale](https://github.com/akinwale) (akinwale@lbry.com)
