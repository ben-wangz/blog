### download kubernetes binary tools

* kind
    + ```shell
      BASE_URL=https://github.com/kubernetes-sigs/kind/releases/download
      # BASE_URL=https://nginx.geekcity.tech/proxy/binary/kind
      curl -LO ${BASE_URL}/v0.11.1/kind-linux-amd64
      curl -LO ${BASE_URL}/v0.11.1/kind-linux-arm64
      curl -LO ${BASE_URL}/v0.11.1/kind-darwin-amd64
      curl -LO ${BASE_URL}/v0.11.1/kind-darwin-arm64
      curl -LO ${BASE_URL}/v0.11.1/kind-windows-amd64
      ```
    + ```shell
      # take kind-linux-amd64 as example
      mv kind-linux-amd64 kind
      chmod 744 kind
      ```
* kubectl
    + ```shell
      BASE_URL=https://dl.k8s.io/release
      # BASE_URL=https://nginx.geekcity.tech/proxy/binary/kubectl
      curl -LO ${BASE_URL}/v1.21.2/bin/linux/amd64/kubectl
      curl -LO ${BASE_URL}/v1.21.2/bin/linux/arm64/kubectl
      curl -LO ${BASE_URL}/v1.21.2/bin/darwin/amd64/kubectl
      curl -LO ${BASE_URL}/v1.21.2/bin/darwin/arm64/kubectl
      curl -LO ${BASE_URL}/v1.21.2/bin/windows/amd64/kubectl.exe
      ```
    + ```shell
      # optional for windows
      chmod 744 kubectl
      ```
* helm
    + ```shell
      BASE_URL=https://get.helm.sh
      # BASE_URL=https://nginx.geekcity.tech/proxy/binary/helm
      curl -LO ${BASE_URL}/helm-v3.6.2-linux-amd64.tar.gz
      curl -LO ${BASE_URL}/helm-v3.6.2-linux-arm64.tar.gz
      curl -LO ${BASE_URL}/helm-v3.6.2-darwin-amd64.tar.gz
      curl -LO ${BASE_URL}/helm-v3.6.2-darwin-arm64.tar.gz
      curl -LO ${BASE_URL}/helm-v3.6.2-windows-amd64.zip
      ```
    + ```shell
      # take linux-amd64 as example
      tar -zxvf helm-v3.6.2-linux-amd64.tar.gz
      mv linux-amd64/helm helm
      rm -rf linux-amd64/
      rm -f helm-v3.6.2-linux-amd64.tar.gz
      chmod 744 helm
      ```