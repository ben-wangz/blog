function download_and_load()
{
    TOPIC_DIRECTORY=$1
    BASE_URL=$2
    IMAGE_LIST="${@:3}"
    # set LOCAL_MODE as 'true' to disable qemu machine mode
    LOCAL_MODE=false

    # prepare directories
    IMAGE_FILE_DIRECTORY_AT_HOST=docker-images/$TOPIC_DIRECTORY
    mkdir -p $IMAGE_FILE_DIRECTORY_AT_HOST
    if [ "true" != "$LOCAL_MODE" ]; then
        IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE=/root/docker-images/$TOPIC_DIRECTORY
        SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
        ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p $IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE"
    fi

    for IMAGE_FILE in $IMAGE_LIST
    do
        IMAGE_FILE_AT_HOST=docker-images/$TOPIC_DIRECTORY/$IMAGE_FILE
        if [ ! -f $IMAGE_FILE_AT_HOST ]; then
            TMP_FILE=$IMAGE_FILE_AT_HOST.tmp
            curl -o $TMP_FILE -L ${BASE_URL}/$TOPIC_DIRECTORY/$IMAGE_FILE
            mv $TMP_FILE $IMAGE_FILE_AT_HOST
        fi
        if [ "true" != "$LOCAL_MODE" ]; then
            IMAGE_FILE_AT_QEMU_MACHINE=$IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE/$IMAGE_FILE
            scp $SSH_OPTIONS -P 10022 $IMAGE_FILE_AT_HOST root@localhost:$IMAGE_FILE_AT_QEMU_MACHINE \
                && ssh $SSH_OPTIONS -p 10022 root@localhost "docker image load -i $IMAGE_FILE_AT_QEMU_MACHINE"
        else
            docker image load -i IMAGE_FILE_AT_HOST
        fi
    done
}