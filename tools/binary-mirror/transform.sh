#! /bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
FILES_LIST=${1:-files.list.example}
VERSION=${2:-1.0.0}
podman run --rm \
    -v $SCRIPT_DIR:/data \
    -v $SCRIPT_DIR/$FILES_LIST:/data/files.list:ro \
    -e DOWNLOAD_PATH=/data/download \
    -it localhost/binary-mirror-transformer:$VERSION