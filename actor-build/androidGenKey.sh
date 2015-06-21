#!/bin/sh

BUILD_DIRECTORY="`pwd`/actor-build"
KEYS_DIR="${BUILD_DIRECTORY}/keys"

echo "######################################"
echo "Generating new signing key for Android"
echo "######################################"

mkdir -p ${KEYS_DIR}
keytool -genkey -v -keystore ${KEYS_DIR}/android.keystore -alias actor_app -keyalg RSA -keysize 2048 -validity 10000

echo "################################################"
echo "Now you can write parameters to android.key file"
echo "with alias name \"actor_app\""
echo "################################################"