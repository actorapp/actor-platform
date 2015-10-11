#!/bin/bash
set -e

SCRIPT_DIR=`pwd`
echo "Actor Platform apps dependency installation in $SCRIPT_DIR"

source "$SCRIPT_DIR/build-tools/common.sh"

INSTALL_ANDROID=true
INSTALL_IOS=${IS_OSX}
INSTALL_WEB=true

if [ "$#" -ne 0 ]; then
	INSTALL_ANDROID=false
	INSTALL_IOS=false
	INSTALL_WEB=false

	for var in "$@"
	do
    	if [ "$var" == "android" ]; then
    		INSTALL_ANDROID=true
    	elif [ "$var" == "web" ]; then
			INSTALL_WEB=true
		elif [ "$var" == "ios" ]; then
			INSTALL_IOS=true
		else 
			echo_w "Unknown argument $var"
			exit 1
		fi			
	done
fi	

UPDATE_GRADLE_PROPERTIES=${INSTALL_ANDROID} || ${INSTALL_IOS}

if $INSTALL_ANDROID; then
	"$SCRIPT_DIR/build-tools/configureAndroidDeps.sh" "${SCRIPT_DIR}"
fi

if $INSTALL_IOS; then 
	"$SCRIPT_DIR/build-tools/configureCocoaDeps.sh" "${SCRIPT_DIR}"
fi	

if $INSTALL_WEB; then 
	"$SCRIPT_DIR/build-tools/configureWebDeps.sh" "${SCRIPT_DIR}"
fi

if $UPDATE_GRADLE_PROPERTIES; then 
	"$SCRIPT_DIR/build-tools/configureGradleProps.sh" "${SCRIPT_DIR}"
fi