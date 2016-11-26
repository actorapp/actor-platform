#!/bin/bash
export BUILD_NUMBER=108
SBT_OPTS="-Xms512M -Xmx2G -Xss2M -XX:+CMSClassUnloadingEnabled"
java $SBT_OPTS -jar `dirname $0`/sbt-launch.jar "$@"
