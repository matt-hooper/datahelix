#!/usr/bin/env bash
if [[ "$#" -ne 1 ]]; then
    echo "Usage: build.sh version"
    exit
fi
mvn -B -DdryRun=true -DreleaseVersion=$1 -DdevelopmentVersion=$1-SNAPSHOT -DautoVersionSubmodules=true release:clean release:prepare
SRC=datahelix-src-$1
cd ..
ROOT=datahelix
ZIP=${SRC}.zip
TAR=${SRC}.tar.gz
touch ${ZIP}
touch ${TAR}
gzip -r ${ZIP} ${ROOT}
tar -czf ${TAR} ${ROOT}