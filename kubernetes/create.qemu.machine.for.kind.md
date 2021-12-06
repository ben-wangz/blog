# create qemu machine for kind

## purpose

* start QEMU virtual machine with KVM(Kernel-based Virtual Machine) accelerator
* the virtual machine is customized for kind
* start basic kind to test it

## pre-requirements

* centos stream 8 operating system with x86_64 chip
* cpu support virtualization(to use KVM accelerator)

## do it

### start centos stream 8 with qemu

1. install qemu
    * [install with linux](../qemu/install.with.linux.md)
    * [install with mac](../qemu/install.with.mac.md)
    * TODO: [install with windows]()
2. in practise
    * download
        + ```shell
          curl -LO https://nginx.geekcity.tech/proxy/qemu/centos.8.qcow2
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
        + note: 10022, 10080 and 10443 is redirected to the ports of qemu-machine

### access the machine started with ssh

1. default `root` password is `123456`
2. configure ssh login without password
    * ```shell
      SSH_PUBLIC_KEY=$(cat /root/.ssh/id_rsa.pub) \
          && ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p 10022 root@localhost \
              "mkdir -p .ssh \
                  && chmod 700 .ssh \
                  && echo '$SSH_PUBLIC_KEY' > .ssh/authorized_keys \
                  && chmod 600 .ssh/authorized_keys"
      ```

### test with kind

1. prepare [all.in.one.8.repo](../kubernetes/resources/all.in.one.8.repo.md)
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
4. [download kubernetes binary tools](../kubernetes/download.kubernetes.binary.tools.md)
    * copy to `/root/bin`
        + ```shell
          SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
              && ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p /root/bin" \
              && scp $SSH_OPTIONS -P 10022 kubectl helm kind root@localhost:/root/bin
          ```
5. prepare docker images
    * download and load docker images
        + ```shell
          TOPIC_DIRECTORY=create.qemu.machine.for.kind
          mkdir -p docker-images/$TOPIC_DIRECTORY
          BASE_URL=https://nginx.geekcity.tech/proxy/docker-images/x86_64
          for IMAGE_FILE in "docker.io_registry_2.7.1.dim" \
              "docker.io_kindest_node_v1.22.1.dim"
          do
              IMAGE_FILE_AT_HOST=docker-images/$TOPIC_DIRECTORY/$IMAGE_FILE
              IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE=/root/docker-images/$TOPIC_DIRECTORY
              IMAGE_FILE_AT_QEMU_MACHINE=$IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE/$IMAGE_FILE
              if [ ! -f $IMAGE_FILE_AT_HOST ]; then
                  TMP_FILE=$IMAGE_FILE_AT_HOST.tmp
                  curl -o $TMP_FILE -L ${BASE_URL}/$TOPIC_DIRECTORY/$IMAGE_FILE
                  mv $TMP_FILE $IMAGE_FILE_AT_HOST
              fi
              SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
                  && ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p $IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE" \
                  && scp $SSH_OPTIONS -P 10022 $IMAGE_FILE_AT_HOST root@localhost:$IMAGE_FILE_AT_QEMU_MACHINE \
                  && ssh $SSH_OPTIONS -p 10022 root@localhost "docker image load -i $IMAGE_FILE_AT_QEMU_MACHINE"
          done
          ```
6. create cluster with a local docker registry
    * prepare [kind.cluster.yaml](resources/kind.cluster.yaml.md)
    * prepare [kind.with.registry.sh](resources/kind.with.registry.sh.md)
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