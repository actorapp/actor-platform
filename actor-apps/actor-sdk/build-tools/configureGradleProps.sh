#!/bin/bash

BUILD_DIRECTORY="$1/build-tools"
APPS_DIRECTORY="$1"
APPS_PROPERTIES="${APPS_DIRECTORY}/local.properties"

# TODO: Move versions and paths to common.sh

rm -f "${APPS_PROPERTIES}"
touch "${APPS_PROPERTIES}"
if [ -d "${BUILD_DIRECTORY}/dist/android-sdk-macosx/" ]; then
	echo "sdk.dir=${BUILD_DIRECTORY}/dist/android-sdk-macosx/" >> "${APPS_PROPERTIES}"
fi

if [ -d "${BUILD_DIRECTORY}/dist/j2objc-0.9.7/" ]; then
	echo "j2objcDir=${BUILD_DIRECTORY}/dist/j2objc-0.9.7/" >> "${APPS_PROPERTIES}"
fi	