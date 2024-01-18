#! /bin/bash

VERSION=${1:-1.0.0}
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
podman build --ulimit nofile=4096:4096 -f $SCRIPT_DIR/Dockerfile -t binary-mirror-transformer:$VERSION $SCRIPT_DIR
