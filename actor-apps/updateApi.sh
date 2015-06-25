#!/bin/sh
set -e

SCRIPT_DIR=`pwd`

cd "$SCRIPT_DIR/core/src/main/java/im/actor/model/api/"
rm -fr "*"

cd "$SCRIPT_DIR/build-tools/codegen-java/"

if [ "$#" -ne 0 ]; then
	"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/../actor-api/actor.json', '$1', '$SCRIPT_DIR/core/src/main/java/']" 
else 
	"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/../actor-api/actor.json', '$SCRIPT_DIR/core/src/main/java/']" 
fi