#!/bin/sh

BUILD_DIRECTORY="`pwd`/actor-build"
APPS_DIRECTORY="`pwd`/actor-apps"
GRADLEW="${APPS_DIRECTORY}/gradlew"

cd "${APPS_DIRECTORY}"
${GRADLEW} ":apps:actor-android:assembleDebug"