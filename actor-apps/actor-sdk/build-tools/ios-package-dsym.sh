# !/bin/bash
set -e

APP=$1
DEST=$2

echo Acrhive dSYM files
cd build
zip -r $DEST.dSYM.zip $APP.app.dSYM
cd ..
