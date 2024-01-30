#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

function pull(){
    CHART_NAME=$1
    CHART_VERSION=$2
    REPO=$3
    DESTINATION=$4
    helm pull \
        --destination ${DESTINATION} \
        --version ${CHART_VERSION} \
        --repo $REPO \
        ${CHART_NAME}
}

DESTINATION=${DESTINATION:-${SCRIPT_DIR}/charts}
CHART_YAML_FILE=${CHART_YAML_FILE:-${SCRIPT_DIR}/charts.yaml}
mkdir -p ${DESTINATION}
yq eval '.[] | ("name=" + .name + "; version=" + .version + "; repo=" + .repo)'  ${CHART_YAML_FILE} \
  | while read -r EXPRESION
    do
        eval $EXPRESION
        pull $name $version $repo ${DESTINATION}
    done
helm repo index ${DESTINATION}
