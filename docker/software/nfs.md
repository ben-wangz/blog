### nfs

* ```shell
  mkdir -p /data/nfs/data
  echo '/data *(rw,fsid=0,no_subtree_check,insecure,no_root_squash)' > /data/nfs/exports
  docker run \
      --name nfs4 \
      --rm \
      --privileged \
      -p 2049:2049 \
      -v /data/nfs/data:/data \
      -v /data/nfs/exports:/etc/exports \
      -d erichough/nfs-server:2.2.1
  ```
* ```shell
  # you may need nfs-utils
  # for centos:
  # yum install nfs-utils
  # for ubuntu:
  # apt-get install nfs-common
  mount -t nfs4 -v localhost:/ /root/mnt/nfs
  ```