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
/usr/libexec/PlistBuddy -c "Set CFBundleVersion $version" ActorClient/Info.plist
echo "##teamcity[progressFinish 'Setting version number']"

# Building Apps

echo "##teamcity[progressStart 'Build Release Version']"
./build.sh AppRelease Actor-AppStore-$version "iPhone Distribution: Stepan Korshakov (ZLY9DP39MF)" "Actor_AppStore"
echo "##teamcity[progressFinish 'Build Release Version']"

echo "##teamcity[progressStart 'Build EAP Version']"
./build.sh AppAlpha Actor-EAP-$version "iPhone Distribution: Ekstradiya OOO" "Extradea_Actor_Enterprise"
echo "##teamcity[progressFinish 'Build EAP Version']"

echo "##teamcity[progressStart 'Build Enteprise Version']"
./build.sh AppEnterprise Actor-Enterprise-$version "iPhone Distribution: Ekstradiya OOO" "Extradea_Actor_Enterprise"
echo "##teamcity[progressFinish 'Build Enteprise Version']"