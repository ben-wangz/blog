#! /bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
podman run --rm \
    -v $SCRIPT_DIR:/code \
    --privileged \
    -it m.daocloud.io/quay.io/containers/buildah:v1.35.4 \
    bash /code/build.sh
