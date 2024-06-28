# container-image-mirror

## prepare

1. [k8s is ready](../../installation/README.md)
    * in this article, the k8s cluster is created by [minikube](../../installation/minikube.md)
2. [argocd is ready and logged in](../../helm/argocd/README.md)
3. [minio is ready](../storage/minio/minio.md)

## logic

* inspired by [wzshiming](https://github.com/wzshiming)
* ![logic-of-container-image-mirror.png](./logic-of-container-image-mirror.png)
* references
    + https://github.com/DaoCloud/crproxy
    + https://docs.docker.com/docker-hub/mirror/
    + https://github.com/twuni/docker-registry.helm

## initialization

1. prepare secret named `s3-credentials-for-registry` to store the minio credentials
    * ```shell
      kubectl -n basic-components create secret generic s3-credentials-for-registry \
        --from-literal=s3AccessKey=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d) \
        --from-literal=s3SecretKey=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      ```
2. create bucket named `registry` in minio
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc mb --ignore-existing minio/mirrors"
      ```

## installation

1. prepare `crproxy.yaml`
    * ```yaml
      <!-- @include: crproxy.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f crproxy.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/crproxy
      ```
4. prepare `registry.yaml`
    * ```yaml
      <!-- @include: registry.yaml -->
      ```
5. apply to k8s
    * ```shell
      kubectl -n argocd apply -f registry.yaml
      ```
6. sync by argocd
    * ```shell
      argocd app sync argocd/registry
      ```
7. if you can't control dns to point `minio-api.dev.geekcity.tech` to `192.168.49.2`
    * patch the deployment by hostAliases
        + ```shell
          kubectl -n basic-components patch deployment registry-docker-registry --patch '
          spec:
            template:
              spec:
                hostAliases:
                - ip: 192.168.49.2
                  hostnames:
                  - minio-api.dev.geekcity.tech
          '
          ```

## tests

1. * `container-image-mirror.dev.geekcity.tech` and `minio-api.dev.geekcity.tech` can be resolved
     + for example
         * add `$K8S_MASTER_IP container-image-mirror.dev.geekcity.tech` to `/etc/hosts`
         * add `$K8S_MASTER_IP minio-api.dev.geekcity.tech` to `/etc/hosts`
     + with k8s with minikube, `$K8S_MASTER_IP` is the ip of the minikube vm, usually `192.168.49.2`
2. pull image
    * ```shell
      podman pull --tls-verify=false container-image-mirror.dev.geekcity.tech:32443/docker.io/library/alpine:3.20.1
      ```

## extensions

1. storage
    * replace minio with oss
    * use pvc
    * without persistent storage
2. network of crproxy
    * set http_proxy and https_proxy for crproxy
3. apply ssl with let's encrypt
