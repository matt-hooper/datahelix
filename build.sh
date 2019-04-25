#!/usr/bin/env bash
if [[ "$#" -ne 1 ]]; then
    echo "Usage: build.sh version"
    exit
fi
mvn -B -DdryRun=true -DreleaseVersion=$1 -DdevelopmentVersion=$1-SNAPSHOT -DautoVersionSubmodules=true release:clean release:prepare
SRC=datahelix-src-$1
cd ..
ROOT=datahelix
TAR=${SRC}.tar.gz
tar -zcf ${TAR} ${ROOT}