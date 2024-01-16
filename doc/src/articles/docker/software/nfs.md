# nfs

1. NFSv4 only
    * server only for root user: `sudo su -`
    * ```shell
      echo -e "nfs\nnfsd" > /etc/modules-load.d/nfs4.conf
      modprobe nfs && modprobe nfsd
      mkdir -p $(pwd)/data/nfs/data
      echo '/data *(rw,fsid=0,no_subtree_check,insecure,no_root_squash)' > $(pwd)/data/nfs/exports
      podman run \
          --name nfs4 \
          --rm \
          --privileged \
          -p 2049:2049 \
          -v $(pwd)/data/nfs/data:/data \
          -v $(pwd)/data/nfs/exports:/etc/exports:ro \
          -d docker.io/erichough/nfs-server:2.2.1
      ```
2. test
    * client is ok for normal user
    * ```shell
      # you may need nfs-utils
      # for centos:
      # yum install nfs-utils
      # for ubuntu:
      # apt-get install nfs-common
      mkdir -p $(pwd)/mnt/nfs
      sudo mount -t nfs4 -v localhost:/ $(pwd)/mnt/nfs
      # you'll see the 'localhost:/' mount point
      df -h
      ```