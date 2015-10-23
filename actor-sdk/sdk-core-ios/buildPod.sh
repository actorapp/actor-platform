#!/bin/zsh

set -e

rm -fr build
mkdir -p build/Output/ActorSDK
mkdir -p build/Output/ActorCore
mkdir -p build/Output/Resources

rsync -avm --include='*.swift' -f 'hide,! */' ActorSDK/Sources/ build/Output/ActorSDK
rsync -avm --include='*.m' -f 'hide,! */' ActorSDK/Sources/ build/Output/ActorSDK
rsync -avm --include='*.h' -f 'hide,! */' ActorSDK/Sources/ build/Output/ActorSDK

rsync -avm --include='*.*' -f 'hide,! */' ActorSDK/Resources/ build/Output/Resources

export PROJECT_DIR=`pwd`
export CONFIGURATION_TEMP_DIR=`pwd`/build/Output/
export PODS_ROOT=`pwd`/Pods

cd ActorSDK/Sources/ActorCore
make translate
cd ../../..

cd build/Output/j2objc/
python "${PROJECT_DIR}/preprocess.py"
cd ../../..

rsync -avm --include='*.m' -f 'hide,! */' build/Output/j2objc/Public/ build/Output/ActorCore
rsync -avm --include='*.h' -f 'hide,! */' build/Output/j2objc/Public/ build/Output/ActorCore