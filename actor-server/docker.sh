#! /bin/bash

./sbt docker:stage && docker build --no-cache=true -f Dockerfile -t actor/server .
