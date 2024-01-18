#! /bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
VERSION=${1:-1.0.0}
DOWNLOAD_FILES_DIRECTORY=$SCRIPT_DIR/download
FILES_LIST_ARIA2=files.list.aria2
mkdir -p $DOWNLOAD_FILES_DIRECTORY
podman run --rm \
    -v $DOWNLOAD_FILES_DIRECTORY:/data/download \
    -v $SCRIPT_DIR/$FILES_LIST_ARIA2:/app/files.list.aria2:ro \
    -it localhost/binary-mirror-downloader:$VERSION