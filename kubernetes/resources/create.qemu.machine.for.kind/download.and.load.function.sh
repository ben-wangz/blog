function download_and_load()
{
    TOPIC_DIRECTORY=$1
    BASE_URL=$2
    IMAGE_LIST="${@:3}"

    # prepare directories
    IMAGE_FILE_DIRECTORY_AT_HOST=docker-images/$TOPIC_DIRECTORY
    mkdir -p $IMAGE_FILE_DIRECTORY_AT_HOST

    for IMAGE_FILE in $IMAGE_LIST
    do
        IMAGE_FILE_AT_HOST=docker-images/$TOPIC_DIRECTORY/$IMAGE_FILE
        if [ ! -f $IMAGE_FILE_AT_HOST ]; then
            TMP_FILE=$IMAGE_FILE_AT_HOST.tmp
            curl -o $TMP_FILE -L ${BASE_URL}/$TOPIC_DIRECTORY/$IMAGE_FILE
            mv $TMP_FILE $IMAGE_FILE_AT_HOST
        fi
        docker image load -i IMAGE_FILE_AT_HOST
    done
}