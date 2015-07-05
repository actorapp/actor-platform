# !/bin/bash

PLIST=$1
KEY=$2
VALUE=$3

/usr/libexec/PlistBuddy -x -c "Set :${KEY} ${VALUE}" $PLIST