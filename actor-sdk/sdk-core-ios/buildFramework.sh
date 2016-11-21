set -e

pod install
pod update

rm -fr build
mkdir -p build/Output

xcodebuild \
  -workspace "ActorSDK.xcworkspace" \
  -scheme "ActorSDK" \
  -derivedDataPath build \
  -arch armv7 -arch armv7s -arch arm64 \
  -sdk iphoneos \
  ONLY_ACTIVE_ARCH=NO \
  -configuration Release \
  -IDEBuildOperationMaxNumberOfConcurrentCompileTasks=4 \
  OTHER_CFLAGS="-fembed-bitcode" \
  build \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO

xcodebuild \
  -workspace "ActorSDK.xcworkspace" \
  -scheme "ActorSDK" \
  -derivedDataPath build \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 6' \
  ONLY_ACTIVE_ARCH=NO \
  -configuration Release \
  -IDEBuildOperationMaxNumberOfConcurrentCompileTasks=4 \
  OTHER_CFLAGS="-fembed-bitcode" \
  build \
  CODE_SIGN_IDENTITY="" \
  CODE_SIGNING_REQUIRED=NO

rm -f build/Output/libactor.so
lipo -create "build/Build/Intermediates/ActorSDK.build/Release-iphoneos/j2objc/Objects/libactor.so" "build/Build/Intermediates/ActorSDK.build/Release-iphonesimulator/j2objc/Objects/libactor.so" -output build/Output/libactor.so

# Building Framework
# Copy base framework
rm -fr build/Output/ActorSDK.framework
cp -a build/Build/Products/Release-iphoneos/ActorSDK.framework build/Output/

# Merging binaries
lipo -create "build/Build/Products/Release-iphoneos/ActorSDK.framework/ActorSDK" "build/Build/Products/Release-iphonesimulator/ActorSDK.framework/ActorSDK" -output build/Output/ActorSDK_Lipo
rm -fr build/Output/ActorSDK.framework/ActorSDK
mv build/Output/ActorSDK_Lipo build/Output/ActorSDK.framework/ActorSDK
rm -fr build/Output/ActorSDK.framework/Frameworks

# Merging swift docs
cp -a build/Build/Products/Release-iphonesimulator/ActorSDK.framework/Modules/ActorSDK.swiftmodule/* build/Output/ActorSDK.framework/Modules/ActorSDK.swiftmodule/

# Copying dSYM
cp -a build/Build/Products/Release-iphoneos/ActorSDK.framework.dSYM/* build/Output/ActorSDK.framework.dSYM/

# Compressing Framework
cd build/Output/
rm -f ActorSDK.zip
zip -r ActorSDK.zip ActorSDK.framework ActorSDK.framework.dSYM

