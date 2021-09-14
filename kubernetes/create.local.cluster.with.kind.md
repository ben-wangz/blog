### create local cluster with kind

1. dependencies
    * kind
        + ```shell
          BASE_URL=https://github.com/kubernetes-sigs/kind/releases/download
          # BASE_URL=https://nginx.geekcity.tech/proxy/binary/kind/
          curl -LO ${BASE_URL}/v0.11.1/kind-linux-amd64
          curl -LO ${BASE_URL}/v0.11.1/kind-linux-arm64
          curl -LO ${BASE_URL}/v0.11.1/kind-darwin-amd64
          curl -LO ${BASE_URL}/v0.11.1/kind-darwin-arm64
          curl -LO ${BASE_URL}/v0.11.1/kind-windows-amd64
          ```
    * kubectl
        + ```shell
          BASE_URL=https://dl.k8s.io/release
          # BASE_URL=https://nginx.geekcity.tech/proxy/binary/kubectl/
          curl -LO ${BASE_URL}/v1.21.2/bin/linux/amd64/kubectl
          curl -LO ${BASE_URL}/v1.21.2/bin/linux/arm64/kubectl
          curl -LO ${BASE_URL}/v1.21.2/bin/darwin/amd64/kubectl
          curl -LO ${BASE_URL}/v1.21.2/bin/darwin/arm64/kubectl
          curl -LO ${BASE_URL}/v1.21.2/bin/windows/amd64/kubectl.exe
          ```
2. configuration
    * [kind.cluster.yaml](resources/kind.cluster.yaml.md)
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
