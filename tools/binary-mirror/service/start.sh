#! /bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
DOWNLOAD_FILES_DIRECTORY=$SCRIPT_DIR/download

podman run --restart always -p 8080:80 \
    --name binary-mirror \
    -v $DOWNLOAD_FILES_DIRECTORY:/usr/share/nginx/html:ro \
    -v $SCRIPT_DIR/default.conf:/etc/nginx/conf.d/default.conf:ro \
    -d docker.io/library/nginx:1.19.9-alpine