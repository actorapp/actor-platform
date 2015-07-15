# !/bin/bash
set -e

APP=$1
CERTIFICATE=$2
PROVISION_FILE=$3
ENTITLEMENTS_FILE=$4

# Adding Provision
cp $PROVISION_FILE "build/Applications/$APP.app/embedded.mobileprovision"

# Extracting entitlements.plist
# security cms -D -i $PROVISION_FILE > build/Applications/profile.plist
# /usr/libexec/PlistBuddy -x -c 'Print:Entitlements' build/Applications/profile.plist > build/Applications/entitlements.plist

# Sign App
cd build/Applications

cd "$APP.app/Frameworks"
SWIFT_LIBS=`find . -name "*dylib"`
for dylib in $SWIFT_LIBS
do
	codesign -f -s "$CERTIFICATE" "${dylib}"
done
cd ../../

codesign --force --sign "$CERTIFICATE" --entitlements "$ENTITLEMENTS_FILE" "$APP.app"
cd ../../