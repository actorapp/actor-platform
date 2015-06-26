# !/bin/bash

PROJECT_PATH=`pwd`
CERTIFICATE=$3
PROVISION=$4

# Build without any codesign
echo "##teamcity[progressStart 'Building $1']"
xctool -workspace ActorApp.xcworkspace -scheme $1 DEPLOYMENT_LOCATION=yes DSTROOT=build DWARF_DSYM_FOLDER_PATH=build CODE_SIGN_IDENTITY="" CODE_SIGNING_REQUIRED=NO clean build
echo "##teamcity[progressFinish 'Building $1']"

# Uncomment for building with codesign and stripping signature
# xcodebuild -workspace ActorApp.xcworkspace -scheme ActorApp DEPLOYMENT_LOCATION=yes DSTROOT=build DWARF_DSYM_FOLDER_PATH=build build
# Remove old mobileprovision
# rm build/Applications/ActorClient.app/embedded.mobileprovision

echo "##teamcity[progressStart 'Provision and Signing $1']"
# Adding Provision
cp BuildConfig/$PROVISION.mobileprovision build/Applications/ActorApp.app/embedded.mobileprovision

# Sign App
# cd build/Applications
codesign --force --sign "$3" --entitlements "BuildConfig/$PROVISION.entitlements.plist" build/Applications/ActorApp.app/Frameworks/*
codesign --force --sign "$3" --entitlements "BuildConfig/$PROVISION.entitlements.plist" build/Applications/ActorApp.app
# cd ../../
echo "##teamcity[progressFinish 'Provision and Signing $1']"

echo "##teamcity[progressStart 'Packaging App $1']"
# Package app
xcrun -sdk iphoneos PackageApplication -v "$PROJECT_PATH/build/Applications/ActorApp.app" -o "$PROJECT_PATH/build/$2.ipa"

# Acrhive dSYM files
cd build
zip -r $2.dSYM.zip ActorApp.app.dSYM
cd ..
echo "##teamcity[progressFinish 'Packaging App $1']"