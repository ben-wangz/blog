# JuiceFS

## sqlite and local filesystem backend(test only)

1. prepare data directory
    * ```shell
      mkdir -p data/backend/myjfs
      mkdir -p data/meta
      ```
2. format a filesystem
    * ```shell
      podman run --rm \
        -v $(pwd)/data/backend/myjfs:/var/jfs/myjfs \
        -v $(pwd)/data/meta:/data/meta \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          juicefs format "sqlite3:///data/meta/myjfs-sqlite3.db" myjfs --storage=file
      ```
3. mount the filesystem
    * ```shell
      podman run --name juicefs --restart always \
        --privileged \
        -v $(pwd)/data/backend/myjfs:/var/jfs/myjfs \
        -v $(pwd)/data/meta:/data/meta \
        -itd m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          juicefs mount "sqlite3:///data/meta/myjfs-sqlite3.db" /mnt
      ```
4. check mounted filesystem
    * ```shell
      podman exec -it juicefs bash -c 'df -h'
      podman exec -it juicefs bash -c 'echo "random string: $(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)" > /mnt/test.txt'
      podman exec -it juicefs bash -c 'cat /mnt/test.txt'
      ```
    * expected output
        + ```text
          Filesystem                         Size  Used Avail Use% Mounted on
          overlay                             60G  6.5G   51G  12% /
          tmpfs                               64M     0   64M   0% /dev
          /dev/mapper/ubuntu--vg-ubuntu--lv   60G  6.5G   51G  12% /data/meta
          tmpfs                              392M  1.8M  390M   1% /etc/hosts
          shm                                 63M     0   63M   0% /dev/shm
          JuiceFS:myjfs                      1.0P     0  1.0P   0% /mnt
          random string: 9VFXrPtnTVtjsSft
          ```
5. mount with another container and check
    * ```shell
      podman kill juicefs && podman rm juicefs
      ```
    * ```shell
      podman run --rm \
        --privileged \
        -v $(pwd)/data/backend/myjfs:/var/jfs/myjfs \
        -v $(pwd)/data/meta:/data/meta \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          bash -c 'juicefs mount --background "sqlite3:///data/meta/myjfs-sqlite3.db" /mnt && cat /mnt/test.txt'
      ```
    * expected output
        + ```text
          random string: 9VFXrPtnTVtjsSft
          ```

## mariadb and minio backend

1. [mariadb is ready](../database/mariadb.md)
    + ```shell
      podman run --rm \
        -e MYSQL_PWD=mysql \
        -it m.daocloud.io/docker.io/library/mariadb:11.2.2-jammy \
        mariadb \
        --host host.containers.internal \
        --port 3306 \
        --user root \
        --execute 'create database myjfs'
      ```
2. [minio is ready](minio.md)
3. format a filesystem
    + ```shell
      podman run --rm \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          juicefs format \
            --storage=minio \
            --bucket http://host.containers.internal:9000/myjfs \
            --access-key minioadmin \
            --secret-key minioadmin \
            "mysql://root:mysql@(host.containers.internal:3306)/myjfs" \
            myjfs 
      ```
4. mount the filesystem and write data
    + ```shell
      podman run --name juicefs-mount-write --rm \
        --privileged \
        -itd m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          bash -c 'juicefs mount --background "mysql://root:mysql@(host.containers.internal:3306)/myjfs" /mnt \
            && df -h \
            && echo "random string: $(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)" > /mnt/test.txt \
            && cat /mnt/test.txt \
            && sleep 1m'
      ```
5. mount by another container and read data
    * ```shell
      podman run --name juicefs-mount-read --rm \
        --privileged \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          bash -c 'juicefs mount --background "mysql://root:mysql@(host.containers.internal:3306)/myjfs" /mnt \
            && df -h \
            && cat /mnt/test.txt'
      ```
6. check the logs of `juicefs-mount-write`
    * ```shell
      podman logs juicefs-mount-write
      ```

## mariadb and oss backend

1. [mariadb is ready](../database/mariadb.md)
    + ```shell
      podman run --rm \
        -e MYSQL_PWD=mysql \
        -it m.daocloud.io/docker.io/library/mariadb:11.2.2-jammy \
        mariadb \
        --host host.containers.internal \
        --port 3306 \
        --user root \
        --execute 'create database myjfsoss'
      ```
2. format a filesystem
    * ```shell
      #export OSS_ACCESS_KEY_ID=your-oss-access-key-id
      #export OSS_ACCESS_KEY_SECRET=your-oss-access-key-secret
      podman run --rm \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          juicefs format \
            --storage=oss \
            --bucket http://data-and-computing-dev.oss-cn-hangzhou-zjy-d01-a.res.cloud.zhejianglab.com/ \
            --access-key $OSS_ACCESS_KEY_ID \
            --secret-key $OSS_ACCESS_KEY_SECRET \
            "mysql://root:mysql@(host.containers.internal:3306)/myjfsoss" \
            myjfs-oss 
      ```
4. mount the filesystem and write data
    + ```shell
      podman run --name juicefs-mount-write --rm \
        --privileged \
        -itd m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          bash -c 'juicefs mount --background "mysql://root:mysql@(host.containers.internal:3306)/myjfsoss" /mnt \
            && df -h \
            && echo "random string: $(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)" > /mnt/test.txt \
            && cat /mnt/test.txt \
            && sleep 1m'
      ```
5. mount by another container and read data
    * ```shell
      podman run --name juicefs-mount-read --rm \
        --privileged \
        -it m.daocloud.io/docker.io/juicedata/mount:ce-v1.2.3 \
          bash -c 'juicefs mount --background "mysql://root:mysql@(host.containers.internal:3306)/myjfsoss" /mnt \
            && df -h \
            && cat /mnt/test.txt'
      ```
6. check the logs of `juicefs-mount-write`
    * ```shell
      podman logs juicefs-mount-write
      ```
