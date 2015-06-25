#!/bin/sh
set -e

SCRIPT_DIR=`pwd`

cd "$SCRIPT_DIR/build-tools/configen/"

# Generate Android
"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-android/src/main/assets/app.json', 'android']"
"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-ios/ActorClient/app.json', 'ios']"
"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-web/src/app.json', 'web']"