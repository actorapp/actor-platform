#!/bin/sh

BUILD_DIRECTORY="`pwd`/actor-build"
APPS_DIRECTORY="`pwd`/actor-apps"
APPS_PROPERTIES="${APPS_DIRECTORY}/local.properties"

# How to make it better??
rm -f "${APPS_PROPERTIES}"
echo "sdk.dir=${BUILD_DIRECTORY}/dist/android-sdk-macosx/" >> "${APPS_PROPERTIES}"
echo "j2objcDir=${BUILD_DIRECTORY}/dist/j2objc-0.9.7/" >> "${APPS_PROPERTIES}"

if [ -d "${BUILD_DIRECTORY}/android.keys" ]; then
	cat "${BUILD_DIRECTORY}/android.keys" >> "${APPS_PROPERTIES}"
fi