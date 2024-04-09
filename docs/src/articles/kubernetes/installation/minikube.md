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
3. configure sudoers to add NOPASSWD to `ben.wangz`
    * ```shell
      # for example
      echo 'ben.wangz ALL=(ALL) NOPASSWD: ALL' >> /etc/sudoers
      ```
4. install podman(use none root user from here)
    * ```shell
      sudo dnf -y install podman
      ```
5. prepare minikube binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_minikube_binary.sh -->
      ```
6. (optional) disable aegis service and reboot system for aliyun
    * https://bugzilla.openanolis.cn/show_bug.cgi?id=5437
    * ```shell
      sudo systemctl disable aegis && sudo reboot
      ```

## install k8s by minikube

1. start minikube
    * ::: code-tabs#shell
      @tab aliyuncs
      ```shell
      minikube start --driver=podman --container-runtime=cri-o --kubernetes-version=v1.27.10 --image-mirror-country=cn --image-repository=registry.cn-hangzhou.aliyuncs.com/google_containers
      ```
      @tab m.daocloud.io
      ```shell
      minikube start --driver=podman --container-runtime=cri-o --kubernetes-version=v1.27.10 --image-mirror-country=cn --image-repository=m.daocloud.io/gcr.io
      ```
      :::
2. add alias of `kubectl`
    * ```shell
      alias kubectl="minikube kubectl --"
      ```
3. may change memory(requires a restart)
    * ```shell
      minikube config set memory 6144
      ```
4. restart minikube
    * ```shell
      minikube stop && minikube start
      ```

## port-forward with ssh tunnel

1. reason
    * minikube is running in a virtual machine, so we need to use ssh tunnel to access the service
2. open ssh tunnel, for example
    * ```shell
      ssh -i ~/.minikube/machines/minikube/id_rsa docker@$(minikube ip) -L '*:30443:0.0.0.0:30443' -N -f
      ssh -i ~/.minikube/machines/minikube/id_rsa docker@$(minikube ip) -L '*:32443:0.0.0.0:32443' -N -f
      ssh -i ~/.minikube/machines/minikube/id_rsa docker@$(minikube ip) -L '*:32080:0.0.0.0:32080' -N -f
      ```
3. better solution with kubectl port-forward
    * ```shell
      kubectl port-forward -n basic-components service/ 30443:30443
      kubectl port-forward -n basic-components service/ 32443:32443
      kubectl port-forward -n basic-components service/ 32080:32080
      ```

## uninstall

1. delete minikube
    * ```shell
      minikube delete --all
      ```

## addtional software

1. optional download `kubectl` binary, then you can use `kubectl` command directly
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_kubectl_binary.sh -->
      ```
2. [install argocd by helm](../helm/argocd/README.md)
    * ```shell
      ssh -i ~/.minikube/machines/minikube/id_rsa docker@$(minikube ip) -L '*:30443:0.0.0.0:30443' -N -f
      ```
3. [install ingress by argocd](../argocd/ingress/README.md)
4. [install cert-manager by argocd](../argocd/cert-manager/README.md)
