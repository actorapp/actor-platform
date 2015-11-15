# Building Actor Core

Actor Core is core libraries for messaging and converted to ObjC and JS sourcces.

### Bulding With Android Studio

Easiest way to work with Actor Core library is just import gradle project from root of this repository in Android Studio

### Building from console (Linux and OSX)

* Before you need to download depdendencies by calling `actor-deps/installCoreDeps.sh`. This will actually download android-sdk.
* Run `./gradlew clean build` from root of repository

### Ubuntu x64

For Ubuntu x64 you need to [install x86 libraries](http://stackoverflow.com/a/23201209/1973243):

```sudo apt-get install lib32stdc++6 lib32z1```
