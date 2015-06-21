#!/bin/sh

BUILD_DIRECTORY="`pwd`/actor-build"
APPS_DIRECTORY="`pwd`/actor-apps"
APPS_PROPERTIES="${APPS_DIRECTORY}/local.properties"

echo "Setting iOS Environment..."

${BUILD_DIRECTORY}/prepareiOSEnv.sh

echo "Setting Android Environment..."

${BUILD_DIRECTORY}/prepareAndroidEnv.sh

echo "Setting Web Environment..."

${BUILD_DIRECTORY}/prepareWebEnv.sh