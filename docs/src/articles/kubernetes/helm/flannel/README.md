# flannel

## prepare

1. k8s is ready
2. helm binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_helm_binary.sh -->
      ```

## install

1. create namespace and configure
    * ```shell
      kubectl create namespace kube-flannel
      kubectl label --overwrite namespace kube-flannel pod-security.kubernetes.io/enforce=privileged
      ```
1. install with helm
    * ```shell
      helm install flannel flannel \
          --namespace kube-flannel \
          --repo https://flannel-io.github.io/flannel/ \
          --set podCidr="10.244.0.0/16" \
          --atomic
      ```
