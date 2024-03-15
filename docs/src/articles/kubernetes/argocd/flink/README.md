# flink-operator

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `flink-operator.yaml`
    * ```yaml
      <!-- @include: flink-operator.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f flink-operator.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/flink-operator
      ```

## deploy flink application

### basic

1. prepare `basic.yaml`
    * ```yaml
      <!-- @include: basic.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl get namespaces flink > /dev/null 2>&1 || kubectl create namespace flink
      kubectl -n flink apply -f basic.yaml
      ```
3. check status with web ui
    * flink.k8s.io should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP flink.k8s.io` to `/etc/hosts`
    * https://flink.k8s.io:32443/flink/basic/

### pyflink

1. build docker image and push to registry
    * optional if you are using the pre-build image: `ghcr.io/ben.wangz/blog-pyflink:main`
    * prepare `docker/Dockerfile`
        + ```text
          <!-- @include: docker/Dockerfile.shadow -->
          ```
    * prepare `docker/python_demo.py`
        + ```python
          <!-- @include: docker/python_demo.py -->
          ```
    * build
        + ```shell
          # cd docker/
          podman build --build-arg PYPI_REPO=https://mirrors.aliyun.com/pypi/simple -t ghcr.io/ben.wangz/blog-pyflink:main .
          ```
2. * prepare `pyflink.yaml`
    * ```yaml
      <!-- @include: pyflink.yaml -->
      ```
3. apply to k8s
    * ```shell
      kubectl get namespaces flink > /dev/null 2>&1 || kubectl create namespace flink
      kubectl -n flink apply -f pyflink.yaml
      ```
4. check status with web ui
    * flink.k8s.io should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP flink.k8s.io` to `/etc/hosts`
    * https://flink.k8s.io:32443/flink/python-example/
