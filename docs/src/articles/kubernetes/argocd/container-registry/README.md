# container-registry

## prepare

1. [k8s is ready](../../installation/README.md)
    * in this article, the k8s cluster is created by [minikube](../../installation/minikube.md)
2. [argocd is ready and logged in](../../helm/argocd/README.md)
3. [minio is ready](../storage/minio/minio.md)

## initialization

1. prepare secret named `s3-credentials-for-container-registry` to store the minio credentials
    * ::: code-tabs#shell
      @tab pvc backend
      ```yaml
      # not required by pvc backend
      ```
      @tab minio backend
      ```yaml
      kubectl -n basic-components create secret generic s3-credentials-for-container-registry \
        --from-literal=s3AccessKey=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d) \
        --from-literal=s3SecretKey=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      ```
      :::
2. create bucket named `container-registry` in minio
    * ::: code-tabs#shell
      @tab pvc backend
      ```yaml
      # not required by pvc backend
      ```
      @tab minio backend
      ```yaml
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc mb --ignore-existing minio/container-registry"
      ```
      :::

## installation

1. prepare `container-registry.yaml`
    * ::: code-tabs#shell
      @tab pvc backend
      ```yaml
      <!-- @include: container-registry-with-pvc-backend.yaml -->
      ```
      @tab minio backend
      ```yaml
      <!-- @include: container-registry-with-pvc-backend.yaml -->
      ```
      :::
    * optional to add password to the container registry
        + generate htpasswd
            * ```shell
              PASSWORD=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
              HTPASSWD=$(podman run --rm --entrypoint htpasswd -it docker.io/library/httpd:2 -Bbn admin $PASSWORD 2>/dev/null)
              ```
            * ```shell
              echo "remember the password which cannot be retrieved again: $PASSWORD"
              echo "corresponding htpasswd: $HTPASSWD"
              ```
        + add `secrets.htpasswd: ${HTPASSWD}` to the `spec.source.helm.values` described in `container-registry.yaml`
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f container-registry.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/container-registry
      ```
4. if you can't control dns to point `minio-api.dev.geekcity.tech` to `${K8S_MASTER_IP}`
    * patch the deployment by hostAliases
        + ```shell
          K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
          kubectl -n basic-components patch deployment container-registry-docker-registry --patch "
          spec:
            template:
              spec:
                hostAliases:
                - ip: ${K8S_MASTER_IP}
                  hostnames:
                  - minio-api.dev.geekcity.tech
          "
          ```

## tests

1. * `container-registry.dev.geekcity.tech` and `minio-api.dev.geekcity.tech` can be resolved
     + for example
         * add `$K8S_MASTER_IP container-registry.dev.geekcity.tech` to `/etc/hosts`
             + ```shell
               echo "$K8S_MASTER_IP container-registry.dev.geekcity.tech" | sudo tee -a /etc/hosts
               ```
         * add `$K8S_MASTER_IP minio-api.dev.geekcity.tech` to `/etc/hosts`
              + ```shell
                echo "$K8S_MASTER_IP minio-api.dev.geekcity.tech" | sudo tee -a /etc/hosts
                ```
     * `$K8S_MASTER_IP` can be retrieved by
         + ```shell
           kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}'
           ```
2. pull image
    * ```shell
      podman pull docker.io/library/alpine:3.20.1
      podman tag docker.io/library/alpine:3.20.1 container-registry.dev.geekcity.tech:32443/alpine:3.20.1
      # $PASSWORD is the password set in the installation step
      podman login --tls-verify=false -u admin -p $PASSWORD container-registry.dev.geekcity.tech:32443
      podman push --tls-verify=false container-registry.dev.geekcity.tech:32443/alpine:3.20.1
      ```
