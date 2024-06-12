#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
mkdir -p $SCRIPT_DIR/build/containers
podman run --rm \
    -v $SCRIPT_DIR/build/containers:/var/lib/containers \
    -v $SCRIPT_DIR:/code \
    --privileged \
    -it m.daocloud.io/quay.io/containers/buildah:v1.35.4 \
    bash /code/build.sh
