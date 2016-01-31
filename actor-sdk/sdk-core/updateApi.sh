#!/bin/bash
set -e

SCRIPT_DIR=`pwd`

cd "$SCRIPT_DIR/core/core-shared/src/main/java/im/actor/core/api"
rm -fr "*"

cd "$SCRIPT_DIR/../sdk-api/api-codegen/"
dist/bin/api-codegen "$SCRIPT_DIR/../sdk-api/actor.json" "$SCRIPT_DIR/core/core-shared/src/main/java/"
