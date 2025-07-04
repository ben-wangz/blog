# minio

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready

## installation

1. prepare secret for root user credentials
    * ```shell
      kubectl get namespaces storage > /dev/null 2>&1 || kubectl create namespace storage
      kubectl -n storage create secret generic minio-credentials \
          --from-literal=rootUser=admin \
          --from-literal=rootPassword=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
2. prepare `minio.yaml`
    * ::: code-tabs#shell
      @tab standalone
      ```yaml
       <!-- @include: minio-standalone.yaml -->
      ```
      @tab distributed
      ```yaml
      <!-- @include: minio-distributed.yaml -->
      ```
      :::
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f minio.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/minio
      ```
5. visit minio console
    * minio-console.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP minio-console.dev.geekcity.tech` to `/etc/hosts`
    * address: http://minio-console.dev.geekcity.tech:32080/login
    * access key: admin
    * access secret
        + ```shell
          kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d && echo
          ```
6. test with `mc`
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc ls minio \
              && mc mb --ignore-existing minio/test \
              && mc cp /etc/hosts minio/test/etc/hosts \
              && mc ls --recursive minio"
      ```

## references
* https://github.com/minio/minio/tree/master/helm/minio
