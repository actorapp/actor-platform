#!/bin/bash
set -e

BUILD_DIRECTORY="${1}/build-tools"

. "$BUILD_DIRECTORY/common.sh"

echo_w "Installing Web Dependencies..."

if $IS_OSX; then
	if ! haveProg brew ; then 
		echo_w "Installing Homebrew..."
		ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
	fi	
	if ! haveProg brew ; then 
		echo_w "Homebrew is not installed: aborting"
		exit 1
	fi

	echo "Installing Node.js..."
	brew install -g node || true

	echo "Installing Bower..."
	npm install -g bower || true

	echo "Installing Gulp..."
	npm install -g gulp || true
else
	echo_w "Installation of Web Dependencies not supported not for OSX"
	exit 1
fi	