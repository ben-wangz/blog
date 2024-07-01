#! /bin/bash

set -e
IMAGE=${1:-s3-integrate-python:latest}
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
BASE_IMAGE=${BASE_IMAGE:-docker.io/library/python:3.12.1-bullseye}
$BUILD_TOOL build \
    --build-arg BASE_IMAGE=$BASE_IMAGE \
    -f $SCRIPT_DIR/Dockerfile \
    -t $IMAGE $SCRIPT_DIR/..
