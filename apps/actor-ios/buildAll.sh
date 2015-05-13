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

echo "##teamcity[progressStart 'Build Llectro Version']"
./build.sh AppLlectro Actor-Llectro-$version "iPhone Distribution: Ekstradiya OOO" "Extradea_Llectro_Preview"
echo "##teamcity[progressFinish 'Build Llectro Version']"