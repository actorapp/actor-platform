# !/bin/bash
set -e

APP=$1
CERTIFICATE=$2
PROVISION_FILE=$3

# Adding Provision
cp $PROVISION_FILE "build/Applications/$APP.app/embedded.mobileprovision"

# Extracting entitlements.plist
security cms -D -i $PROVISION_FILE > build/Applications/profile.plist
/usr/libexec/PlistBuddy -x -c 'Print:Entitlements' build/Applications/profile.plist > build/Applications/entitlements.plist
# plutil -convert binary1 build/Applications/entitlements.plist

# Sign App
cd build/Applications
cd "$APP.app/Frameworks"

SWIFT_LIBS=`find . -name "*dylib"`
# SDK_PATH="/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphoneos/"
for dylib in $SWIFT_LIBS
do
	# rm "${dylib}"
	# cp -v "/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphoneos/${dylib}" .
	codesign -f -s "$CERTIFICATE" "${dylib}"
done
cd ../../

codesign --force --sign "$CERTIFICATE" --entitlements entitlements.plist "$APP.app"
cd ../../