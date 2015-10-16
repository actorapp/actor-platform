#!/bin/bash
set -e

BUILD_DIRECTORY="${1}/build-tools"

. "$BUILD_DIRECTORY/common.sh"

echo "Installing Web Dependencies..."

if $IS_OSX; then

	if ! haveProg brew ; then
		echo "Installing Homebrew..."
		ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
	else
		echo "Homebrew already installed. Skiping..."
	fi

	if ! haveProg node ; then
		echo "Installing Node.js..."
		brew install node || true
	else
		echo "Node.js already installed. Skiping..."
	fi

	if ! haveProg gulp ; then
		echo "Installing Gulp..."
		npm install -g gulp || true
	else
		echo "Gulp already installed. Skiping..."
	fi

else
	echo "Installation of Web Dependencies not supported not for OSX"
	exit 1
fi
