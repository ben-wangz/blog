function load_image()
{
    LOCAL_REPOSITORY=$1
    IMAGE_LIST="${@:2}"

    for IMAGE in $IMAGE_LIST
    do
        LOCAL_IMAGE="$LOCAL_REPOSITORY/$IMAGE"
        docker image inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
        docker image tag $IMAGE $LOCAL_IMAGE
        docker push $LOCAL_IMAGE
    done
}