### create local cluster with kind

1. [download kubernetes binary tools](download.kubernetes.binary.tools.md)
    * kind
    * kubectl
2. configuration
    * [kind.cluster.yaml](resources/kind/kind.cluster.yaml.md)
3. create cluster
    * ```shell
      kind create cluster --config $(pwd)/kind.cluster.yaml
      ```
4. check with kubectl
    * ```shell
      kubectl get node -o wide
      kubectl get pod --all-namespaces
      ```
5. delete cluster
    * ```shell
      kind delete cluster
      ```

### useful commands

1. check images loaded by node
    + ```shell
      docker exec -it kind-control-plane crictl images
      ```
