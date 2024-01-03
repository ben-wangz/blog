# maria-db

## main usage

* a relational database instead of mysql

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `maria-db`
* install `maria-db-tool`
* test maria-db with `maria-db-tool`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="maria.db.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_mysqld-exporter_0.13.0-debian-10-r56.dim"
      ```
4. install `maria-db`
    * prepare [maria.db.values.yaml](resources/maria.db/maria.db.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/mariadb:10.5.12-debian-10-r0" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r153" \
              "docker.io/bitnami/mysqld-exporter:0.13.0-debian-10-r56"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-maria-db \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/mariadb-9.4.2.tgz \
              --values maria.db.values.yaml \
              --atomic
          ```
5. install `maria-db-tool`
    * prepare [maria.db.tool.yaml](resources/maria.db/maria.db.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f maria.db.tool.yaml
      ```

## test maria-db with maria-db-tool

1. connect to database
    * ```shell
      kubectl -n application exec -it deployment/maria-db-tool \
          -- bash -c '\
              echo "show databases; show variables like \"slow_query%\"" \
                  | mysql -h my-maria-db-mariadb.application -uroot -p$MARIADB_ROOT_PASSWORD my_database'
      ```

## uninstallation

1. uninstall `maria-db-tool`
    * ```shell
      kubectl -n application delete -f maria.db.tool.yaml
      ```
2. uninstall `maria-db`
    * ```shell
      helm -n application uninstall my-maria-db \
          && kubectl -n application delete pvc data-my-maria-db-mariadb-0
      ```


