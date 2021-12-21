# jupyterhub

## main usage

* jupyterhub

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `jupyterhub`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="jupyterhub.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_jupyterhub_1.5.0-debian-10-r34.dim" \
          "docker.io_bitnami_configurable-http-proxy_4.5.0-debian-10-r146.dim" \
          "docker.io_bitnami_jupyter-base-notebook_1.5.0-debian-10-r34.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r281.dim" \
          "docker.io_bitnami_postgresql_11.14.0-debian-10-r17.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r265.dim" \
          "docker.io_bitnami_postgres-exporter_0.10.0-debian-10-r133.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install jupyterhub
    * prepare [jupyterhub.values.yaml](resources/jupyterhub/jupyterhub.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/jupyterhub:1.5.0-debian-10-r34" \
              "docker.io/bitnami/configurable-http-proxy:4.5.0-debian-10-r146" \
              "docker.io/bitnami/jupyter-base-notebook:1.5.0-debian-10-r34" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r281" \
              "docker.io/bitnami/postgresql:11.14.0-debian-10-r17" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r265" \
              "docker.io/bitnami/postgres-exporter:0.10.0-debian-10-r133"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-jupyterhub \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/jupyterhub-0.3.4.tgz \
              --values jupyterhub.values.yaml \
              --atomic
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: jupyterhub.local' https://localhost
      ```
2. visit gitea via website
    * configure hosts
        + ```shell
          echo $QEMU_HOST_IP jupyterhub.local >> /etc/hosts
          ```
    * visit `https://jupyterhub.local:10443/` with your browser

## uninstallation

1. uninstall `jupyterhub`
    * ```shell
      helm -n application uninstall my-jupyterhub \
          && kubectl -n application delete pvc data-my-jupyterhub-postgresql-0
      ```
