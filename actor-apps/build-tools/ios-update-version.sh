# !/bin/bash

VERSION=$1
PLIST=$2

baseVersion=$(/usr/libexec/PlistBuddy -c "Print CFBundleShortVersionString" ${PLIST})

versionParts=(${baseVersion//./ })
partsCount=`expr ${#versionParts[@]} - 2`
version=""
for i in $(seq 0 ${partsCount}); do 
	version="${version}${versionParts[$i]}."
done
version="${version}${VERSION}"

echo ${version}

/usr/libexec/PlistBuddy -x -c "Set :CFBundleShortVersionString ${version}" $PLIST
/usr/libexec/PlistBuddy -x -c "Set :CFBundleVersion ${version}" $PLIST