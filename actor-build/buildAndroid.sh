#!/bin/sh

BUILD_DIRECTORY="`pwd`/actor-build"
APPS_DIRECTORY="`pwd`/actor-apps"
GRADLEW="${APPS_DIRECTORY}/gradlew"

# Updating configuration parameters
${BUILD_DIRECTORY}/updateAppsConfig.sh

# Building Release Android app
cd "${APPS_DIRECTORY}"
${GRADLEW} ":apps:actor-android:assembleRelease"