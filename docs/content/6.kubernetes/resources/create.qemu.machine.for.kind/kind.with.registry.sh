#!/bin/sh
set -o errexit

KIND_CLUSTER_CONFIGURATION=${1:-kind.cluster.yaml}
KIND_BINARY=${2:-kind}
KUBECTL_BINARY=${3:-kubectl}
REGISTRY_NAME=${4:-kind-registry}
REGISTRY_PORT=${5:-5000}
KIND_IMAGE=${6:-kindest/node:v1.22.1}

running="$(docker inspect -f '{{.State.Running}}' "${REGISTRY_NAME}" 2>/dev/null || true)"
if [ "${running}" != 'true' ]; then
    DOCKER_REGISTRY_IMAGE=registry:2.7.1
    docker inspect $DOCKER_REGISTRY_IMAGE > /dev/null 2>&1 || docker pull $DOCKER_REGISTRY_IMAGE
    docker run --restart=always \
        -p "127.0.0.1:${REGISTRY_PORT}:5000" \
        --name "${REGISTRY_NAME}" \
        -d $DOCKER_REGISTRY_IMAGE
fi

docker inspect $KIND_IMAGE > /dev/null 2>&1 || docker pull $KIND_IMAGE
$KIND_BINARY create cluster --image $KIND_IMAGE --config=${KIND_CLUSTER_CONFIGURATION}

docker network connect "kind" "${REGISTRY_NAME}" || true

cat <<EOF | $KUBECTL_BINARY apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: local-registry-hosting
  namespace: kube-public
data:
  localRegistryHosting.v1: |
    host: "localhost:${REGISTRY_PORT}"
    help: "https://kind.sigs.k8s.io/docs/user/local-registry/"
EOF