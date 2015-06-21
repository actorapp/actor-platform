#!/bin/sh

ANDROID_SDK_VERSION="r24.3.3"
BUILD_DIRECTORY="`pwd`/actor-build"
DIST_DIR="${BUILD_DIRECTORY}/dist/"
SDK_DIR="${BUILD_DIRECTORY}/dist/android-sdk-macosx/"
SDK_COMPONENTS="tools,build-tools-22.0.1,android-22,extra-google-m2repository,extra-android-m2repositor,extra-android-support"

echo Downloading Android SDK...
curl -o "${BUILD_DIRECTORY}/android_sdk.zip" http://dl.google.com/android/android-sdk_${ANDROID_SDK_VERSION}-macosx.zip

echo Extracting Android SDK...
mkdir -p "${DIST_DIR}"
unzip -q "${BUILD_DIRECTORY}/android_sdk.zip" -d "${DIST_DIR}"
rm "${BUILD_DIRECTORY}/android_sdk.zip"

echo Installing Android SDK Components...
${SDK_DIR}/tools/android update sdk --no-ui --filter ${SDK_COMPONENTS}