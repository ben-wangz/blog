# calico(tigera-operator)

## prepare

1. k8s is ready
2. helm binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_helm_binary.sh -->
      ```

## install

1. install with helm
    * ```shell
      helm install \
          --create-namespace --namespace tigera-operator \
          tigera-operator tigera-operator \
          --repo https://ben-wangz.github.io/helm-chart-mirror/charts \
          --version v3.27.0 \
          --set tigeraOperator.registry=m.daocloud.io/ \
          --set calicoctl.image=m.daocloud.io/docker.io/calico/ctl \
          --atomic
      ```
