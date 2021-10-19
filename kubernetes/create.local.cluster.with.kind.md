### create local cluster with kind

1. be sure your machine have 2 cores and 4G memory at least
2. [download kubernetes binary tools](download.kubernetes.binary.tools.md)
    * kind
    * kubectl
3. configuration
    * [kind.cluster.yaml](resources/kind/kind.cluster.yaml.md)
4. create cluster
    * ```shell
      kind create cluster --config $(pwd)/kind.cluster.yaml
      ```
5. check with kubectl
    * ```shell
      kubectl get node -o wide
      kubectl get pod --all-namespaces
      ```
6. delete cluster
    * ```shell
      kind delete cluster
      ```

### useful commands

1. check images loaded by node
    + ```shell
      docker exec -it kind-control-plane crictl images
      ```
