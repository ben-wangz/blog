# minikube

## references

1. [minikube](https://minikube.sigs.k8s.io/docs/start/)

## prepare

1. fedora os 38
2. use non root user, for example `ben.wangz`
    * ```shell
      # for example
      useradd ben.wangz
      ```
3. configure sudoers to add NOPASSWD to `/usr/bin/podman`
    * ```shell
      # for example
      echo 'ben.wangz ALL=(ALL) NOPASSWD: /usr/bin/podman' >> /etc/sudoers
      ```
4. install podman(use none root user from here)
    * ```shell
      sudo dnf -y install podman
      ```
5. prepare minikube binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_minikube_binary.sh -->
      ```

## install k8s by minikube

1. start minikube
    * ::: code-tabs#shell
      @tab aliyun
      ```shell
      minikube start --driver=podman --container-runtime=cri-o --kubernetes-version=v1.27.10 --image-repository=registry.cn-hangzhou.aliyuncs.com/google_containers
      ```
      @tab m.daocloud.io
      ```shell
      minikube start --driver=podman --container-runtime=cri-o --kubernetes-version=v1.27.10 --image-repository=m.daocloud.io/gcr.io
      ```
      :::
2. add alias of `kubectl`
    * ```shell
      alias kubectl="minikube kubectl --"
      ```
3. may change memory(requires a restart)
    * ```shell
      minikube config set memory 9001
      ```
4. restart minikube
    * ```shell
      minikube stop && minikube start
      ```

## uninstall

1. delete minikube
    * ```shell
      minikube delete --all
      ```

## addtional software
1. [install argocd by helm](../helm/argocd/README.md)
2. [install ingress by argocd](../argocd/ingress/README.md)
3. [install cert-manager by argocd](../argocd/cert-manager/README.md)
