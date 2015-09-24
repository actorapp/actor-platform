#!/bin/bash
set -e

SCRIPT_DIR=`pwd`

# cd "$SCRIPT_DIR/core/src/main/java/im/actor/model/api/"
# rm -fr "*"

cd "$SCRIPT_DIR/build-tools/codegen-java/"
dist/bin/codegen-java "$SCRIPT_DIR/../actor-api/actor.json" "$SCRIPT_DIR/core/src/main/java/"
