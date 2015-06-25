#!/bin/sh
set -e

echo "Actor Platform apps dependency installation..."

SCRIPT_DIR="$(dirname "$0")"
source "$SCRIPT_DIR/build/common.sh"

INSTALL_ANDROID=true
INSTALL_IOS=${IS_OSX}
INSTALL_WEB=true

if $INSTALL_ANDROID; then
	# echo_w "Installing Android environment..."
	"$SCRIPT_DIR/build/configureAndroidDeps.sh" "${SCRIPT_DIR}"
fi

if $INSTALL_IOS; then 
	# echo_w "Installing iOS environment..."
	"$SCRIPT_DIR/build/configureCocoaDeps.sh" "${SCRIPT_DIR}"
fi	

if $INSTALL_WEB; then 
	# echo_w "Installing Web environment..." "${SCRIPT_DIR}"
	"$SCRIPT_DIR/build/configureWebDeps.sh" "${SCRIPT_DIR}"
fi
# echo "Installing Android environment: ${INSTALL_ANDROID}"
# echo "Installing iOS environment: ${INSTALL_IOS}"
# echo "Installing Web environment: ${INSTALL_WEB}"