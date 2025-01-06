#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
IMAGE=${IMAGE:-localhost/ben.wangz/classify-handwritten-digits:latest}
BASE_IMAGE=${BASE_IMAGE:-docker.io/pytorch/pytorch:2.5.1-cuda12.4-cudnn9-devel}
TLS_VERIFY=${TLS_VERIFY:-true}
buildah --tls-verify=${TLS_VERIFY} build \
    --build-arg BASE_IMAGE=${BASE_IMAGE} \
    -f $SCRIPT_DIR/Dockerfile \
    -t $IMAGE $SCRIPT_DIR/..
