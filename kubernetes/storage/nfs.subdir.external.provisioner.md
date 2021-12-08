# nfs subdir external provisioner

## main usage

* a provisioner for nfs storage

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* setup nfs4 service by docker
* install `nfs-subdir-external-provisioner`
* test provisioner with `maria-db`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="nfs.subdir.external.provisioner"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "k8s.gcr.io_sig-storage_nfs-subdir-external-provisioner_v4.0.2.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_mysqld-exporter_0.13.0-debian-10-r56.dim"
      ```
3. setup nfs4 service by docker
    * TODO change to deployment
    * ```shell
      mkdir -p $(pwd)/data/nfs/data \
          && echo '/data *(rw,fsid=0,no_subtree_check,insecure,no_root_squash)' > $(pwd)/data/nfs/exports \
          && modprobe nfs && modprobe nfsd \
          && docker run \
              --name nfs4 \
              --rm \
              --privileged \
              -p 2049:2049 \
              -v $(pwd)/data/nfs/data:/data \
              -v $(pwd)/data/nfs/exports:/etc/exports:ro \
              -d docker.io/erichough/nfs-server:2.2.1
      ```
4. configure `/etc/hosts` for `nfs4.service.docker.local`
    * ```shell
      echo 172.17.0.1 nfs4.service.docker.local >> /etc/hosts \
          && docker exec kind-control-plane bash -c 'echo 172.17.0.1 nfs4.service.docker.local >> /etc/hosts' 
      ```
5. install `nfs-subdir-external-provisioner`
    * prepare [nfs.subdir.external.provisioner.values.yaml
      ](resources/nfs.subdir.external.provisioner/nfs.subdir.external.provisioner.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "k8s.gcr.io/sig-storage/nfs-subdir-external-provisioner:v4.0.2"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace nfs-provisioner \
              my-nfs-subdir-external-provisioner \
              nfs-subdir-external-provisioner \
              --version 4.0.14 \
              --repo https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner \
              --values nfs.subdir.external.provisioner.values.yaml \
              --atomic
          ```

## test provisioner with maria-db

1. install `maria-db`
    * prepare [test.maria.db.values.yaml](resources/nfs.subdir.external.provisioner/test.maria.db.values.yaml.md)
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
              --create-namespace --namespace test \
              my-maria-db \
              mariadb \
              --version 9.4.2 \
              --repo https://charts.bitnami.com/bitnami \
              --values test.maria.db.values.yaml \
              --atomic
          ```
2. connect to database
    * ```shell
      kubectl run my-maria-db-mariadb-client \
          -n test \
          --rm --tty -i --restart='Never' \
          --image insecure.docker.registry.local:80/docker.io/bitnami/mariadb:10.5.12-debian-10-r0 \
          --env MARIADB_ROOT_PASSWORD=$(kubectl get secret --namespace test my-maria-db-mariadb \
              -o jsonpath="{.data.mariadb-root-password}" \
              | base64 --decode)\
          -- bash -c '\
              echo "show databases; show variables like \"slow_query%\"" \
                  | mysql -h my-maria-db-mariadb.test -uroot -p$MARIADB_ROOT_PASSWORD my_database'
      ```
3. clean `maria-db`
    * ```shell
      helm -n test uninstall my-maria-db \
          && kubectl -n test delete pvc data-my-maria-db-mariadb-0
      ```
