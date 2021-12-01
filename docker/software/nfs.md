### nfs

1. NFSv4 only
    + ```shell
      mkdir -p $(pwd)/data/nfs/data
      echo '/data *(rw,fsid=0,no_subtree_check,insecure,no_root_squash)' > $(pwd)/data/nfs/exports
      modprobe nfs && modprobe nfsd
      docker run \
          --name nfs4 \
          --rm \
          --privileged \
          -p 2049:2049 \
          -v $(pwd)/data/nfs/data:/data \
          -v $(pwd)/data/nfs/exports:/etc/exports:ro \
          -d erichough/nfs-server:2.2.1
      ```
2. test
    * ```shell
      # you may need nfs-utils
      # for centos:
      # yum install nfs-utils
      # for ubuntu:
      # apt-get install nfs-common
      mkdir -p $(pwd)/mnt/nfs \
          && mount -t nfs4 -v localhost:/ $(pwd)/mnt/nfs
      ```