# postgresql

## main usage

* an open source object-relational database known for reliability and data integrity

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `postgresql`
* install `postgresql-tool`
* test postgresql with `postgresql-tool`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="postgresql.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_postgresql_14.4.0-debian-11-r9.dim" \
          "docker.io_bitnami_bitnami-shell_11-debian-11-r14.dim" \
          "docker.io_bitnami_postgres-exporter_0.10.1-debian-11-r14.dim"
      ```
4. install `postgresql`
    * prepare [postgresql.values.yaml](resources/postgresql/postgresql.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/postgresql:14.4.0-debian-11-r9" \
              "docker.io/bitnami/bitnami-shell:11-debian-11-r14" \
              "docker.io/bitnami/postgres-exporter:0.10.1-debian-11-r14"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-postgresql \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/postgresql-11.6.16.tgz \
              --values postgresql.values.yaml \
              --atomic
          ```
5. install `postgresql-tool`
    * prepare [postgresql.tool.yaml](resources/postgresql/postgresql.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f postgresql.tool.yaml
      ```

## test postgresql with postgresql-tool

1. connect to database
    * ```shell
      kubectl -n application exec -it deployment/postgresql-tool \
          -- bash -c '\
              echo "\\list" \
                  | PGPASSWORD="$POSTGRES_PASSWORD" psql --host my-postgresql.application -U postgres -d postgres -p 5432'
      ```

## uninstallation

1. uninstall `postgresql-tool`
    * ```shell
      kubectl -n application delete -f postgresql.tool.yaml
      ```
2. uninstall `postgresql`
    * ```shell
      helm -n application uninstall my-postgresql \
          && kubectl -n application delete pvc data-my-postgresql-0
      ```


