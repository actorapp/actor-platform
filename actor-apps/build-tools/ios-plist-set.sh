# !/bin/bash
set -e

KEY=$1
VALUE=$2
PLIST=$3

/usr/libexec/PlistBuddy -x -c "Set :${KEY} ${VALUE}" "$PLIST"