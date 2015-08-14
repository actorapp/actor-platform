#!/bin/bash

SCRIPT_DIR=`pwd`

./gradlew ":core-cocoa:build" ":runtime-cocoa:build"

cd app-ios
pod install
cd ..