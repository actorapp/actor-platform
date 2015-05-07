# !/bin/bash

# Installing CocoaPods
echo "##teamcity[progressStart 'Installing CocoaPods']"
pod install
echo "##teamcity[progressFinish 'Installing CocoaPods']"

# Installing BuildVersion
echo "##teamcity[progressStart 'Setting version number']"
baseVersion=$(/usr/libexec/PlistBuddy -c "Print CFBundleShortVersionString" ActorClient/Info.plist)
version=$baseVersion.$1
echo "##teamcity[buildNumber '$version']"
/usr/libexec/PlistBuddy -c "Set CFBundleShortVersionString $version" ActorClient/Info.plist
/usr/libexec/PlistBuddy -c "Set CFBundleVersion %build.counter%" ActorClient/Info.plist
echo "##teamcity[progressFinish 'Setting version number']"

# Building Apps

echo "##teamcity[progressStart 'Build Release Version']"
./build.sh AppRelease Actor-$version
echo "##teamcity[progressFinish 'Build Release Version']"

echo "##teamcity[progressStart 'Build EAP Version']"
./build.sh AppAlpha Actor-Alpha-$version
echo "##teamcity[progressFinish 'Build EAP Version']"

echo "##teamcity[progressStart 'Build Enteprise Version']"
./build.sh AppEnterprise Actor-Enterprise-$version
echo "##teamcity[progressFinish 'Build Enteprise Version']"

# echo "##teamcity[progressStart 'Build Dev1 Version']"
# ./build.sh AppDev1 Actor-Dev1-$version
# echo "##teamcity[progressFinish 'Build Dev1 Version']"

# echo "##teamcity[progressStart 'Build Dev2 Version']"
# ./build.sh AppDev2 Actor-Dev2-$version
# echo "##teamcity[progressFinish 'Build Dev2 Version']"