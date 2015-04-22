# !/bin/bash

PROJECT_PATH=`pwd`

# Build without any codesign
xcodebuild -workspace ActorApp.xcworkspace -scheme $1 DEPLOYMENT_LOCATION=yes DSTROOT=build DWARF_DSYM_FOLDER_PATH=build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO clean build

# Uncomment for building with codesign and stripping signature
# xcodebuild -workspace ActorApp.xcworkspace -scheme ActorApp DEPLOYMENT_LOCATION=yes DSTROOT=build DWARF_DSYM_FOLDER_PATH=build build
# Remove old mobileprovision
# rm build/Applications/ActorClient.app/embedded.mobileprovision

# Adding Provision
cp BuildConfig/Messenger_InHouse.mobileprovision build/Applications/ActorApp.app/embedded.mobileprovision

# Sign App
# cd build/Applications
codesign --force --sign 'iPhone Distribution: Ekstradiya OOO' --entitlements BuildConfig/Messenger_InHouse.entitlements.plist build/Applications/ActorApp.app/Frameworks/*
codesign --force --sign 'iPhone Distribution: Ekstradiya OOO' --entitlements BuildConfig/Messenger_InHouse.entitlements.plist build/Applications/ActorApp.app
# cd ../../

# Package app
xcrun -sdk iphoneos PackageApplication -v "$PROJECT_PATH/build/Applications/ActorApp.app" -o "$PROJECT_PATH/build/$2.ipa"

# Acrhive dSYM files
cd build
zip $2.dSYM.zip ActorApp.app.dSYM
cd ..