#!/bin/sh
set -e

BUILD_DIR=$1
CONFIG_DIR=$2
APP=$3
VARIANT=$4
BUILD_COUNTER=$5

# Including yaml parser
. $BUILD_DIR/build-tools/yaml.sh

################################################################################
echo "##teamcity[blockOpened name='Loading config']"
echo "##teamcity[message text='Loading confgiguration']"
# Parsing settings
eval $(parse_yaml "$CONFIG_DIR/apps.yaml" "config_")

# Bundle Title. Used for result bundle file name.
APP_TITLE=$(eval "echo \$$(echo config_apps_${APP}_info_title)")

# Fabric configuration
APP_FABRIC_API=$(eval "echo \$$(echo config_apps_${APP}_build_fabric_api_key)")
APP_FABRIC_SECRET=$(eval "echo \$$(echo config_apps_${APP}_build_fabric_build_secret)")

# Application signing configuration
APP_SIGNING_VENDOR=$(eval "echo \$$(echo config_apps_${APP}_ios_variants_${VARIANT}_build_key_vendor)")
APP_SIGNING_KEY=$(eval "echo \$$(echo config_apps_${APP}_ios_variants_${VARIANT}_build_key_key)")

# Loading config
APP_IDENTITY=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_identity)")
APP_CERTIFICATE=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_certificate)")
APP_BUNDLE_ID=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_keys_${APP_SIGNING_KEY}_bundle)")
APP_PROVISION=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_keys_${APP_SIGNING_KEY}_provision)")
APP_ENTITLEMENTS=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_keys_${APP_SIGNING_KEY}_entitlements)")
APP_APP_STORE=$(eval "echo \$$(echo config_ios_signing_${APP_SIGNING_VENDOR}_keys_${APP_SIGNING_KEY}_appstore)")

# Starting build
echo Building $APP_BUNDLE_ID $APP_IDENTITY with $CONFIG_DIR/$APP_PROVISION and $CONFIG_DIR/$APP_ENTITLEMENTS
echo "##teamcity[blockClosed name='Loading config']"

################################################################################
echo "##teamcity[blockOpened name='Installing j2objc']"
echo "##teamcity[message text='Installing j2objc...']"
# Go to build directory
cd $BUILD_DIR
# Installing depdendencies
./installDeps.sh ios
echo "##teamcity[blockClosed name='Installing j2objc']"

################################################################################
echo "##teamcity[blockOpened name='Configuring app.json']"

build-tools/configen/dist/bin/configen "$CONFIG_DIR/apps.yaml" "ios" "$APP" "$VARIANT" "$BUILD_DIR/app-ios/ActorApp/Supporting Files/app.json"

echo "##teamcity[blockClosed name='Configuring app.json']"

################################################################################
echo "##teamcity[blockOpened name='Configuring Info.plist']"

# Setting bundle id
echo "##teamcity[message text='Setting bundle id: $APP_BUNDLE_ID']"
build-tools/ios-plist-set.sh "CFBundleIdentifier" "$APP_BUNDLE_ID" "$BUILD_DIR/app-ios/ActorApp/Supporting Files/Info.plist"

# Setting fabric api key
build-tools/ios-plist-set.sh ":Fabric:APIKey" "$APP_FABRIC_API" "$BUILD_DIR/app-ios/ActorApp/Supporting Files/Info.plist"

# Setting build counter
if [ ! -z "$BUILD_COUNTER" ]; then
  echo "##teamcity[message text='Setting build counter: $BUILD_COUNTER']"
  APP_VERSION=$(eval "build-tools/ios-update-version.sh \"$BUILD_COUNTER\" \"$BUILD_DIR/app-ios/ActorApp/Supporting Files/Info.plist\"")
  APP_ARCHIVE_NAME=$APP_TITLE-$VARIANT-$APP_VERSION
  echo "##teamcity[buildNumber '$APP_VERSION-$VARIANT']"
else
  APP_ARCHIVE_NAME=$APP_TITLE
fi
echo "##teamcity[blockClosed name='Configuring Info.plist']"

################################################################################
# Go to ios app directory
cd $BUILD_DIR/app-ios/

################################################################################
echo "##teamcity[blockOpened name='Installing CocoaPods']"
# Installing cocoa pods
pod install
echo "##teamcity[blockClosed name='Installing CocoaPods']"

################################################################################
echo "##teamcity[blockOpened name='Building app']"

# Clearing build dir
rm -fr build

# Building app without sign
xcodebuild \
  -workspace "ActorApp.xcworkspace" \
  -scheme "ActorApp" \
  FABRIC_BUILD_SECRET=$APP_FABRIC_SECRET \
  DEPLOYMENT_LOCATION=yes \
  DSTROOT=build \
  DWARF_DSYM_FOLDER_PATH=build \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO \
  clean \
  build

echo "##teamcity[blockClosed name='Building app']"

################################################################################
echo "##teamcity[blockOpened name='Signing build']"
# Signing build
../build-tools/ios-sign.sh ActorApp "$APP_IDENTITY" "$CONFIG_DIR/$APP_PROVISION" "$CONFIG_DIR/$APP_ENTITLEMENTS"
echo "##teamcity[blockClosed name='Signing build']"

################################################################################
echo "##teamcity[blockOpened name='Packaging app']"

# Packaging build
echo "##teamcity[message text='Packaging IPA']"
../build-tools/ios-package.sh ActorApp "$BUILD_DIR/app-ios/build/$APP_ARCHIVE_NAME.ipa"

if [ "$APP_APP_STORE" = true ] ; then
  echo "##teamcity[message text='Adding Swift libraries']"
  ../build-tools/ios-copy-swift.sh "$APP_ARCHIVE_NAME.ipa" ActorApp
fi

# Packaging dsym
echo "##teamcity[message text='Packaging dSYM']"
../build-tools/ios-package-dsym.sh ActorApp "$APP_ARCHIVE_NAME"
echo "##teamcity[blockClosed name='Packaging app']"
