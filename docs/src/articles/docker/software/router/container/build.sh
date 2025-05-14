#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

IMAGE=${IMAGE:-localhost/router:latest}
podman build -t $IMAGE -f $SCRIPT_DIR/Dockerfile $SCRIPT_DIR
