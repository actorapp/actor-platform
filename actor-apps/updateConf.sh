#!/bin/bash
set -e

SCRIPT_DIR=`pwd`

cd "$SCRIPT_DIR/build-tools/configen/"

# Generate Android
if [ "$#" -ne 0 ]; then
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$1', '$SCRIPT_DIR/app-android/src/main/assets/app.json', 'android']"
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$1', '$SCRIPT_DIR/app-ios/ActorClient/app.json', 'ios']"
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$1', '$SCRIPT_DIR/app-web/src/app.json', 'web']"
else
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-android/src/main/assets/app.json', 'android']"
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-ios/ActorClient/app.json', 'ios']"
    "./gradlew" run  "-PappArgs=['$SCRIPT_DIR/app.conf', '$SCRIPT_DIR/app-web/src/app.json', 'web']"
fi