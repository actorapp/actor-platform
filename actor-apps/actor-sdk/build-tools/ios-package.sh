# !/bin/bash
set -e

APP=$1
DEST_IPA=$2

# Package app
xcrun -sdk iphoneos PackageApplication -v "build/Applications/$APP.app" -o "$DEST_IPA"

echo Acrhive dSYM files
cd build
zip -r $APP.dSYM.zip $APP.app.dSYM
cd ..