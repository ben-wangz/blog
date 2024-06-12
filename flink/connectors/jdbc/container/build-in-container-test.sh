#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
mkdir -p /tmp/build/containers
mkdir -p /tmp/build/gradle
podman run --rm \
    -v /tmp/build/containers:/var/lib/containers \
    -v /tmp/build/.gradle:/root/.gradle \
    -v $SCRIPT_DIR/../../../..:/code \
    --privileged \
    -it m.daocloud.io/quay.io/containers/buildah:v1.35.4 \
    bash /code/flink/connectors/jdbc/container/build.sh
