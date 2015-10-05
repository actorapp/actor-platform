#!/bin/bash

# DETECT OS TYPE

IS_OSX=false
IS_LINUX=false

if [[ "$OSTYPE" == "darwin"* ]]; then
	IS_OSX=true
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
	IS_LINUX=true
elif [[ "$OSTYPE" == "cygwin" ]]; then
    echo "Windows in unsupported"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    echo "Windows in unsupported"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "Windows in unsupported"
    exit 1
elif [[ "$OSTYPE" == "freebsd"* ]]; then
	echo "FreeBSD in unsupported"
	exit 1
else
    echo "{$OSTYPE} in unsupported"
    exit 1
fi

readonly IS_OSX
readonly IS_LINUX
export IS_OSX
export IS_LINUX

# COLORS

function echo_w {
    echo "\033[0;31m$1\033[0m"
}

function haveProg() {
    [ -x "$(which $1)" ]
}

# WARRING='\033[0;33m'
# NORMAL='\033[0m' # No Color