#! /bin/bash

set -e
IMAGE=${1:-build-container-demo:1.0.0}
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
$BUILD_TOOL build \
    --build-arg BASE_IMAGE=m.daocloud.io/docker.io/library/alpine:3.20 \
    -f $SCRIPT_DIR/Dockerfile \
    -t $IMAGE $SCRIPT_DIR
