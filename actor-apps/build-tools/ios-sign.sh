# !/bin/bash

APP=$1
CERTIFICATE=$2
PROVISION_FILE=$3

# Adding Provision
cp $PROVISION_FILE "build/Applications/$APP.app/embedded.mobileprovision"

# Extracting entitlements.plist
security cms -D -i $PROVISION_FILE > build/Applications/profile.plist
/usr/libexec/PlistBuddy -x -c 'Print:Entitlements' build/Applications/profile.plist > build/Applications/entitlements.plist
plutil -convert binary1 build/Applications/entitlements.plist

# Sign App
cd build/Applications
codesign --force --sign "$CERTIFICATE" --entitlements entitlements.plist "$APP.app/Frameworks/*"
codesign --force --sign "$CERTIFICATE" --entitlements entitlements.plist "$APP.app"
cd ../../

# Package app
xcrun -sdk iphoneos PackageApplication -v "build/Applications/$APP.app" -o "$DEST_IPA"

Acrhive dSYM files
cd build
zip -r $APP.dSYM.zip $APP.app.dSYM
cd ..