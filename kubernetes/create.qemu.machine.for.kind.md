# create qemu machine for kind

## purpose

* start QEMU virtual machine with KVM(Kernel-based Virtual Machine) accelerator
* the virtual machine is customized for kind
* start basic kind to test it

## pre-requirements

* centos stream 8 operating system with x86_64 chip
* cpu support virtualization(to use KVM accelerator)

## installation

### start centos stream 8 with qemu

1. install qemu
    * [install with linux](../qemu/install.with.linux.md)
    * [install with mac](../qemu/install.with.mac.md)
    * TODO: [install with windows]()
2. in practise
    * download
        + ```shell
          curl -LO https://resource.geekcity.tech/qemu/centos.8.qcow2
          ```
    * start with qemu
        + ```shell
          qemu-system-x86_64 \
              -accel kvm \
              -cpu kvm64 -smp cpus=2 \
              -m 8G \
              -drive file=centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
              -rtc base=localtime \
              -pidfile centos.8.qcow2.pid \
              -display none \
              -nic user,hostfwd=tcp::10022-:22,hostfwd=tcp::10080-:80,hostfwd=tcp::10443-:443 \
              -daemonize
          ```
        + note: 10022, 10080 and 10443 are redirected to the ports of qemu-machine

### access the machine started with ssh

1. (optional) generate ssh keys if not exists
    * ```shell
      ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa
      ```
2. default `root` password is `123456`
3. configure ssh login without password
    * ```shell
      SSH_PUBLIC_KEY=$(cat /root/.ssh/id_rsa.pub) \
          && ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost \
              "mkdir -p .ssh \
                  && chmod 700 .ssh \
                  && echo '$SSH_PUBLIC_KEY' > .ssh/authorized_keys \
                  && chmod 600 .ssh/authorized_keys"
      ```

### create kind cluster

1. prepare [all.in.one.8.repo](resources/create.qemu.machine.for.kind/all.in.one.8.repo.md)
2. replace yum repositories
    * ```shell
      REPO_CONTENT=$(cat all.in.one.8.repo) \
          && ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost \
              "rm -rf /etc/yum.repos.d/* && echo '$REPO_CONTENT' > /etc/yum.repos.d/all.in.one.8.repo"
      ```
3. install docker
    * ```shell
      ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost \
          "dnf -y install tar yum-utils device-mapper-persistent-data lvm2 docker-ce \
              && systemctl enable docker \
              && systemctl start docker"
      ```
    * pre-configure docker-registry to support `insecure.docker.registry.local:80` which may be needed later
        + prepare [docker.daemon.json](resources/create.qemu.machine.for.kind/docker.daemon.json.md)
        + ```shell
          SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
              && scp $SSH_OPTIONS -P 10022 docker.daemon.json root@localhost:/etc/docker/daemon.json \
              && ssh $SSH_OPTIONS -p 10022 root@localhost "systemctl restart docker"
          ```
4. [download kubernetes binary tools](../kubernetes/download.kubernetes.binary.tools.md)
    * copy to `/root/bin`
        + ```shell
          SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
              && ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p /root/bin" \
              && scp $SSH_OPTIONS -P 10022 kubectl helm kind root@localhost:/root/bin
          ```
5. prepare docker images
    * run scripts
      in [download.and.load.function.sh](resources/create.qemu.machine.for.kind/download.and.load.function.sh.md)
    * ```shell
      TOPIC_DIRECTORY="create.qemu.machine.for.kind"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_registry_2.7.1.dim" \
          "docker.io_kindest_node_v1.22.1.dim"
      ```
6. create cluster with a local docker registry
    * prepare [kind.cluster.yaml](resources/create.qemu.machine.for.kind/kind.cluster.yaml.md)
    * prepare [kind.with.registry.sh](resources/create.qemu.machine.for.kind/kind.with.registry.sh.md)
    * create cluster
        + ```shell
          SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
              && ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p /root/bin /root/conf" \
              && scp $SSH_OPTIONS -P 10022 kind.with.registry.sh root@localhost:/root/bin \
              && scp $SSH_OPTIONS -P 10022 kind.cluster.yaml root@localhost:/root/conf \
              && ssh $SSH_OPTIONS -p 10022 root@localhost \
                  "bash /root/bin/kind.with.registry.sh /root/conf/kind.cluster.yaml /root/bin/kind /root/bin/kubectl"
          ```
    * checking
        + ```shell
          ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost \
              "/root/bin/kubectl -n kube-system wait --for=condition=ready pod --all \
                  && /root/bin/kubectl get pod --all-namespaces"
          ```
7. login with ssh and enjoy
    * ```shell
      ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost
      ```