#!/bin/bash

cd ..
./gradlew  actor-keygen:assembleDist
cd actor-keygen

# Unpacking Distrib
cd build
rm -fr docker
mkdir -p docker
cd distributions
rm -fr actor-keygen
unzip actor-keygen.zip
cp -r actor-keygen/* ../docker/
cd ../..

# Building docker
docker build -t actor/keygen .