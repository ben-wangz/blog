#! /bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
IMAGE_LIST_FILE=${1:-$SCRIPT_DIR/images.list}
DOWNLOAD_IMAGES_DIRECTORY=$SCRIPT_DIR/download
mkdir -p $DOWNLOAD_IMAGES_DIRECTORY
while read IMAGE
do
    FILE_NAME="$(echo $IMAGE | sed s@"/"@"-"@g | sed s/":"/"-"/g)".dim
    IMAGE_FILE=$DOWNLOAD_IMAGES_DIRECTORY/$FILE_NAME
    crictl inspecti $IMAGE > /dev/null || crictl pull $IMAGE
    crictl save -o $IMAGE_FILE $IMAGE
done < $IMAGE_LIST_FILE