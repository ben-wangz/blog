#! /bin/bash

set -e
IMAGE=${1:-flink-connectors-multiple-s3:latest}
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
if type buildah > /dev/null 2>&1; then 
    BUILD_TOOL=buildah
elif type podman > /dev/null 2>&1; then
    BUILD_TOOL=podman
elif type docker > /dev/null 2>&1; then
    BUILD_TOOL=docker
else
    echo "no build tool found"
    exit 1
fi
BUILD_IMAGE=${BUILD_IMAGE:-docker.io/library/gradle:8.5.0-jdk11-jammy}
BASE_IMAGE=${BASE_IMAGE:-docker.io/library/flink:1.19}
$BUILD_TOOL build \
    --build-arg BUILD_IMAGE=$BUILD_IMAGE \
    --build-arg BASE_IMAGE=$BASE_IMAGE \
    -f $SCRIPT_DIR/Dockerfile \
    -t $IMAGE $SCRIPT_DIR/../../../..
