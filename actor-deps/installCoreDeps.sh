#!/bin/bash
set -e

ANDROID_SDK_VERSION="r24.4"
BUILD_DIRECTORY="`pwd`/actor-deps"
DIST_DIR="${BUILD_DIRECTORY}/dist"

. "$BUILD_DIRECTORY/common.sh"

if $IS_OSX; then
	echo "Running in OSX Mode"
	SDK_DIR="${BUILD_DIRECTORY}/dist/android-sdk-macosx"
else
	echo "Running in Linux Mode"
	SDK_DIR="${BUILD_DIRECTORY}/dist/android-sdk-linux"	
fi	
SDK_FORCE_COMPONENTS="build-tools-23.0.2,build-tools-23.0.1,build-tools-22.0.1,build-tools-21.1.2,extra-android-m2repository"
SDK_LEVELS="android-23,android-22,android-21"
SDK_COMPONENTS="tools,platform-tools,${SDK_LEVELS},extra-google-m2repository,extra-android-support,${SDK_FORCE_COMPONENTS}"
SDK_TOOL="${SDK_DIR}/tools/android"

echo "Android SDK Dir: ${SDK_DIR}"
echo "Android SDK Tool: ${SDK_TOOL}"

if [ ! -d "${SDK_DIR}" ]; then
	echo_w Downloading Android SDK...
	if $IS_OSX; then
		curl -o "${BUILD_DIRECTORY}/android_sdk.zip" http://dl.google.com/android/android-sdk_${ANDROID_SDK_VERSION}-macosx.zip
	elif $IS_LINUX; then 
		curl -o "${BUILD_DIRECTORY}/android_sdk.tgz" http://dl.google.com/android/android-sdk_${ANDROID_SDK_VERSION}-linux.tgz	
	else
		exit 1
	fi

	echo Extracting Android SDK...
	mkdir -p "${DIST_DIR}"
	if $IS_OSX; then
		unzip -q "${BUILD_DIRECTORY}/android_sdk.zip" -d "${DIST_DIR}"
		rm "${BUILD_DIRECTORY}/android_sdk.zip"
	elif $IS_LINUX; then 
		tar zxvf "${BUILD_DIRECTORY}/android_sdk.tgz" -C "${DIST_DIR}"
		rm "${BUILD_DIRECTORY}/android_sdk.tgz"
	else
		exit 1
	fi
else 
	echo_w "Android SDK Already installed. Skiping..."
fi

echo Installing Android SDK Components...
# ${SDK_TOOL} list sdk --all --extended --no-ui
( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | "${SDK_TOOL}" update sdk --no-ui --filter "${SDK_COMPONENTS}"
# Hack for propper installation
( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | "${SDK_TOOL}" update sdk -a --no-ui --filter "${SDK_FORCE_COMPONENTS}"

if $IS_OSX; then
echo "sdk.dir=actor-deps/dist/android-sdk-macosx" > local.properties
else
echo "sdk.dir=actor-deps/dist/android-sdk-linux" > local.properties
fi	