# gitea

## main usage

* service for git repositories

## conceptions

* none

## practise

### pre-requirements

* none

### purpose

* create a kubernetes cluster by kind
* setup ingress-nginx
* install gitea

### do it

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="gitea.software"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_memcached_1.6.9-debian-10-r114.dim" \
          "docker.io_bitnami_memcached-exporter_0.8.0-debian-10-r105.dim" \
          "docker.io_bitnami_postgresql_11.11.0-debian-10-r62.dim" \
          "docker.io_bitnami_bitnami-shell_10.dim" \
          "docker.io_bitnami_postgres-exporter_0.9.0-debian-10-r34.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install gitea
    * prepare [gitea.values.yaml](resources/gitea/gitea.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/memcached:1.6.9-debian-10-r114" \
              "docker.io/bitnami/memcached-exporter:0.8.0-debian-10-r105" \
              "docker.io/bitnami/postgresql:11.11.0-debian-10-r62" \
              "docker.io/bitnami/bitnami-shell:10" \
              "docker.io/bitnami/postgres-exporter:0.9.0-debian-10-r34"
          ```
    * create `gitea-admin-secret`
        + ```shell
          # uses the "Array" declaration
          # referencing the variable again with as $PASSWORD an index array is the same as ${PASSWORD[0]}
          ./bin/kubectl get namespace application \
              || ./bin/kubectl create namespace application
          PASSWORD=($((echo -n $RANDOM | md5sum 2>/dev/null) || (echo -n $RANDOM | md5 2>/dev/null)))
          # NOTE: username should have at least 6 characters
          ./bin/kubectl -n application \
              create secret generic gitea-admin-secret \
              --from-literal=username=gitea_admin \
              --from-literal=password=$PASSWORD
          ```
    * install by helm
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace application \
              my-gitea \
              gitea \
              --version 4.1.1 \
              --repo https://dl.gitea.io/charts \
              --values gitea.values.yaml \
              --atomic
          ```
5. visit gitea via website
    * ```shell
      curl --insecure https://gitea.local
      ```
6. visit gitea via ssh
    * ```shell
      
      ```