#!/bin/sh

# Configuration

BUILD_DIRECTORY="`pwd`/actor-build"
GWT_VERSION="2.7.0"
GWT_DIR="${BUILD_DIRECTORY}/dist/"
GWT_DIST_URL="http://goo.gl/t7FQSn"

# Google Web Toolkit

echo "Downloading GWT..."
curl -o "${BUILD_DIRECTORY}/gwt-${GWT_VERSION}.zip" -L ${GWT_DIST_URL}

echo "Extracting GWT..."
mkdir -p "${GWT_DIR}"
unzip -q "${BUILD_DIRECTORY}/gwt-${GWT_VERSION}.zip" -d "${GWT_DIR}"
rm "${BUILD_DIRECTORY}/gwt-${GWT_VERSION}.zip"

# Required Web Tools

echo "Installing Node.js..."
brew install node

echo "Installing Bower..."
npm install -g bower

echo "Installing Gulp..."
npm install -g gulp