# pgadmin4

## main usage

* control panel for postgres

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `pgadmin4`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="pgadmin4.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_dpage_pgadmin4_6.11.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install `pgadmin4`
    * prepare [pgadmin4.values.yaml](resources/pgadmin4/pgadmin4.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/dpage/pgadmin4:6.11"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-pgadmin4 \
              https://resource.geekcity.tech/kubernetes/charts/https/helm.runix.net/pgadmin4-1.11.0.tgz \
              --values pgadmin4.values.yaml \
              --atomic
          ```

## test

1. test connection
    * ```shell
      curl --insecure --header 'Host: pgadmin4.local' https://localhost
      ```

## uninstallation

1. uninstall `pgadmin4`
    * ```shell
      helm -n application uninstall my-pgadmin4
      ```
