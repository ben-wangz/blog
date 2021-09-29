### create centos 8 with qemu

1. create step by step
    * [install with linux](install.with.linux.md)
    * [install with mac](install.with.mac.md)
    * TODO: [install with windows]()
2. if you do not want to install system
    * install qemu
    * download disk already installed
    * start machine with qemu
3. in practise(take Ubuntu 20.04.2 LTS as an example)
    * download
        + ```shell
          curl -LO https://nginx.geekcity.tech/proxy/qemu/centos.8.qcow2
          ```
    * start with qemu
        + ```shell
          qemu-system-x86_64 \
              -accel kvm \
              -cpu kvm64 -smp cpus=1 \
              -m 1G \
              -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
              -rtc base=localtime \
              -pidfile $(pwd)/centos.8.qcow2.pid \
              -display none \
              -nic user,hostfwd=tcp::1022-:22 \
              -daemonize
          ```
        + note: choose your own accelerator, in this example we use `-accel kvm`
    * login with ssh
        + ```shell
          ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost
          ```
        + default password is `123456`