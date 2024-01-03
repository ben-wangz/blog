# phpmyadmin

## main usage

* a relational database instead of mysql

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `phpMyAdmin`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="php.my.admin.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_phpmyadmin_5.1.1-debian-10-r147.dim" \
              "docker.io_bitnami_apache-exporter_0.10.1-debian-10-r54.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install `phpMyAdmin`
    * prepare [php.my.admin.values.yaml](resources/php.my.admin/php.my.admin.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/phpmyadmin:5.1.1-debian-10-r147" \
              "docker.io/bitnami/apache-exporter:0.10.1-debian-10-r54"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-phpmyadmin \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/phpmyadmin-8.3.1.tgz \
              --values php.my.admin.values.yaml \
              --atomic
          ```

## test maria-db with maria-db-tool

1. test connection
    * ```shell
      curl --insecure --header 'Host: phpmyadmin.local' https://localhost
      ```

## uninstallation

1. uninstall `phpMyAdmin`
    * ```shell
      helm -n application uninstall my-phpmyadmin
      ```
