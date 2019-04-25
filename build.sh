#!/usr/bin/env bash
if [[ "$#" -ne 1 ]]; then
    echo "Usage: build.sh version"
    exit
fi

# Get latest tag, assume this is the version.
CURRENT="$(git describe --tags --abbrev=0)"

echo ${CURRENT}

# Update the version based on the history

echo "$sql"

HISTORY="$(git log ${CURRENT}..HEAD --pretty="%s")"

echo ${HISTORY}

mvn -B -DdryRun=true -DreleaseVersion=$1 -DdevelopmentVersion=$1-SNAPSHOT -DautoVersionSubmodules=true release:clean release:prepare
SRC=datahelix-src-$1
cd ..
ROOT=datahelix
TAR=${SRC}.tar.gz
tar -zcf ${TAR} ${ROOT}