#!/bin/sh
set -o errexit

KIND_CLUSTER_CONFIGURATION=${1:-kind.cluster.yaml}
KIND_BINARY=${2:-kind}
KUBECTL_BINARY=${3:-kubectl}
REGISTRY_NAME=${4:-kind-registry}
REGISTRY_PORT=${5:-5000}

# create registry container unless it already exists
running="$(docker inspect -f '{{.State.Running}}' "${REGISTRY_NAME}" 2>/dev/null || true)"
if [ "${running}" != 'true' ]; then
  docker run \
    --restart=always \
    -p "127.0.0.1:${REGISTRY_PORT}:5000" \
    --name "${REGISTRY_NAME}" \
    -d registry:2
fi

# create a cluster with the local registry enabled in containerd
$KIND_BINARY create cluster --image kindest/node:v1.22.1 --config=${KIND_CLUSTER_CONFIGURATION}

# connect the registry to the cluster network
# (the network may already be connected)
docker network connect "kind" "${REGISTRY_NAME}" || true

# Document the local registry
# https://github.com/kubernetes/enhancements/tree/master/keps/sig-cluster-lifecycle/generic/1755-communicating-a-local-registry
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