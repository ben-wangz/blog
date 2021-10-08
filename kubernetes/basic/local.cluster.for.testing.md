# local cluster for testing

## main usage

* create a local k8s cluster for testing

## conceptions

* none

## practise

### pre-requirements

* [a k8s cluster created by kind](../create.local.cluster.with.kind.md) have been read and practised
* [download kubernetes binary tools](../download.kubernetes.binary.tools.md)
    + kind
    + kubectl
    + helm
* we recommend to use [qemu machine](../../qemu/README.md)

### purpose

* create a kubernetes cluster by kind
* setup docker registry

### do it

1. optional, [create centos 8 with qemu](../../qemu/create.centos.8.with.qemu.md)
    * ```shell
      qemu-system-x86_64 \
          -accel kvm \
          -smp cpus=3 \
          -m 5G \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -rtc base=localtime \
          -pidfile $(pwd)/centos.8.qcow2.pid \
          -display none \
          -nic user,hostfwd=tcp::1022-:22 \
          -daemonize
      ```
    * login with ssh
        + ```shell
          ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost
          ```
        + default password is `123456`
    * optional, replace yum repositories with [all.in.one.8.repo](../resources/all.in.one.8.repo.md)
    * install docker
        + ```shell
          dnf -y install tar yum-utils device-mapper-persistent-data lvm2 docker-ce \
              && systemctl enable docker \
              && systemctl start docker
          ```
2. [download kubernetes binary tools](../download.kubernetes.binary.tools.md)
    * link tools to `./bin`
3. create cluster with a docker registry
    * prepare [kind-with-registry.sh](resources/kind-with-registry.sh.md)
        + `kind.cluster.yaml` will be created by script.
    * create cluster
        + ```shell
          bash kind-with-registry.sh ./bin/kind ./bin/kubectl
          ```
    * checking
        + ```shell
          ./bin/kubectl get pod
          ```
