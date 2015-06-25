#!/bin/sh
set -e

SCRIPT_DIR=`pwd`

# rm -fr "$SCRIPT_DIR/core/src/main/java/im/actor/model/api/"

echo "$SCRIPT_DIR/build-tools/codegen-java/gradlew"

cd "$SCRIPT_DIR/build-tools/codegen-java/"
"./gradlew" run  "-PappArgs=['$SCRIPT_DIR/../actor-api/actor.json', '$SCRIPT_DIR/core/src/main/java/']" 