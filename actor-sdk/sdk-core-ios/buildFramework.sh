set -e

pod install

rm -fr build
mkdir -p build/Output

xcodebuild \
  -workspace "ActorSDK.xcworkspace" \
  -scheme "ActorSDK" \
  -derivedDataPath build \
  -arch armv7 -arch armv7s -arch arm64 \
  -sdk iphoneos9.0 \
  ONLY_ACTIVE_ARCH=NO \
  -configuration Release \
  -IDEBuildOperationMaxNumberOfConcurrentCompileTasks=4 \
  build

xcodebuild \
  -workspace "ActorSDK.xcworkspace" \
  -scheme "ActorSDK" \
  -derivedDataPath build \
  -arch x86_64 \
  -sdk iphonesimulator9.0 \
  ONLY_ACTIVE_ARCH=NO \
  -configuration Release \
  -IDEBuildOperationMaxNumberOfConcurrentCompileTasks=4 \
  build

rm build/Output/libactor.so
lipo -create "build/Build/Intermediates/ActorSDK.build/Release-iphoneos/j2objc/Objects/libactor.so" "build/Build/Intermediates/ActorSDK.build/Release-iphonesimulator/j2objc/Objects/libactor.so" -output build/Output/libactor.so

# Building Framework
# Copy base framework
rm -fr build/Output/ActorSDK.framework
cp -a build/Build/Products/Release-iphoneos/ActorSDK.framework build/Output/

# Merging binaries
lipo -create "build/Build/Products/Release-iphoneos/ActorSDK.framework/ActorSDK" "build/Build/Products/Release-iphonesimulator/ActorSDK.framework/ActorSDK" -output build/Output/ActorSDK_Lipo
rm build/Output/ActorSDK.framework/ActorSDK
mv build/Output/ActorSDK_Lipo build/Output/ActorSDK.framework/ActorSDK
rm -f build/Output/ActorSDK.framework/Frameworks/*

# Merging swift docs
cp -a build/Build/Products/Release-iphonesimulator/ActorSDK.framework/Modules/ActorSDK.swiftmodule/* build/Output/ActorSDK.framework/Modules/ActorSDK.swiftmodule/

# Copying dSYM
cp -a build/Build/Products/Release-iphoneos/ActorSDK.framework.dSYM/* build/Output/ActorSDK.framework.dSYM/

# Compressing Framework
cd build/Output/
rm -f ActorSDK.zip
zip -r ActorSDK.zip ActorSDK.framework ActorSDK.framework.dSYM

