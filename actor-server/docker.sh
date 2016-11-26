#! /bin/bash
export BUILD_NUMBER=108
./sbt docker:stage && docker build --no-cache=true -f Dockerfile -t actor/server .
