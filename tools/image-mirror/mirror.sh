#! /bin/bash

set -e
set -x
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

TARGET_REGISTRY=${TARGET_REGISTRY:-docker.io}
REGISTRY_USERNAME=${REGISTRY_USERNAME:-anonymous}
REGISTRY_PASSWORD=${REGISTRY_PASSWORD:-password4anonymous}
REPOSITORY_PREFIX=${REPOSITORY_PREFIX:-}
IMAGE_YAML_FILE=${IMAGE_YAML_FILE:-${SCRIPT_DIR}/images.yaml}
KEEP_STRUCTURE=${KEEP_STRUCTURE:-true}
STRING_TO_REPLACE_SLASH=${STRING_TO_REPLACE_SLASH:-_}
PUSH_WITH_TLS_VERIFY=$PUSH_WITH_TLS_VERIFY:-true}
podman login --tls-verify=false -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD} ${TARGET_REGISTRY}
yq eval '.[] | ("registry=" + .registry + "; repository=" + .repository + "; tag=" + .tag)'  ${IMAGE_YAML_FILE} \
  | while read -r EXPRESION
    do
        eval $EXPRESION
        SOURCE_IMAGE=${registry}/${repository}:${tag}
        if [ "${KEEP_STRUCTURE}" = "true" ]; then
            TARGET_IMAGE=${TARGET_REGISTRY}/${REPOSITORY_PREFIX}${SOURCE_IMAGE}
        else
            TARGET_IMAGE=${TARGET_REGISTRY}/${REPOSITORY_PREFIX}$(echo ${SOURCE_IMAGE} | sed -e "s/\//${STRING_TO_REPLACE_SLASH}/g")
        fi
        podman image pull ${SOURCE_IMAGE}
        podman image tag ${SOURCE_IMAGE} ${TARGET_IMAGE}
        podman push --tls-verify=${PUSH_WITH_TLS_VERIFY} ${TARGET_IMAGE}
    done
