# busybox

## prepare

1. k8s is ready

## installation

1. prepare `busybox.yaml`
    * ::: code-tabs#shell
      @tab fedora
      ```yaml
      <!-- @include: fedora.yaml -->
      ```
      @tab busybox
      ```yaml
      <!-- @include: busybox.yaml -->
      ```
      :::
2. apply to k8s
    * ```shell
      kubectl apply -f busybox.yaml
      ```
3. exec
    * ::: code-tabs#shell
      @tab fedora
      ```shell
      kubectl exec -it deployment/fedora -- bash
      ```
      @tab busybox
      ```shell
      kubectl exec -it deployment/busybox -- sh
      ```
      :::
4. use wget to test http service
    * ```shell
      wget -q -O - https://minio-console.storage:9001
      ```