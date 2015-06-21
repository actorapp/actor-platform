#!/bin/sh

ANDROID_SDK_VERSION="r24.3.3"
BUILD_DIRECTORY="`pwd`/actor-build"
DIST_DIR="${BUILD_DIRECTORY}/dist/"
SDK_DIR="${BUILD_DIRECTORY}/dist/android-sdk-macosx/"
SDK_FORCE_COMPONENTS="build-tools-22.0.1,build-tools-21.1.2,extra-android-m2repository"
SDK_LEVELS="android-22,android-21"
SDK_COMPONENTS="tools,platform-tools,${SDK_LEVELS},extra-google-m2repository,extra-android-support"
SDK_TOOL="${SDK_DIR}/tools/android"

if [ ! -d "${SDK_DIR}" ]; then
	echo Downloading Android SDK...
	curl -o "${BUILD_DIRECTORY}/android_sdk.zip" http://dl.google.com/android/android-sdk_${ANDROID_SDK_VERSION}-macosx.zip

	echo Extracting Android SDK...
	mkdir -p "${DIST_DIR}"
	unzip -q "${BUILD_DIRECTORY}/android_sdk.zip" -d "${DIST_DIR}"
	rm "${BUILD_DIRECTORY}/android_sdk.zip"
fi

echo Installing Android SDK Components...
# ${SDK_TOOL} list sdk --all --extended --no-ui
( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | ${SDK_TOOL} update sdk --no-ui --filter "${SDK_COMPONENTS}"
# Hack for propper installation
( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | ${SDK_TOOL} update sdk -a --no-ui --filter "${SDK_FORCE_COMPONENTS}"