# install qemu with linux

## purpose

* start QEMU virtual machine with KVM(Kernel-based Virtual Machine) accelerator

## pre-requirements

* linux operating system with x86_64 chip(take Ubuntu 20.04.2 LTS as an example)
* cpu support virtualization(to use KVM accelerator)

## do it

### install qemu binary

* Ubuntu 20.04.2 LTS
    + ```shell
      apt-get -y install qemu-kvm
      ```
* CentOS 8
    + ```shell
      dnf install qemu-kvm
      ```

### check your cpu

* 'vmx' or 'svm' is expected
    + ```shell
      cat /proc/cpuinfo | egrep "vmx|svm" | uniq
      ```
* 'kvm' is expected
    + ```shell
      qemu-system-x86_64 -accel help
      ```

### start qemu

1. create virtual disk
    * ```shell
      qemu-img create -f qcow2 centos.8.qcow2 40G
      ```
2. download boot image of centos 8
    * ```shell
      curl -LO https://mirrors.aliyun.com/centos/8.4.2105/isos/x86_64/CentOS-8.4.2105-x86_64-boot.iso
      ```
3. start qemu machine with boot image and the virtual disk created
    * ```shell
      qemu-system-x86_64 \
          -accel kvm \
          -cpu kvm64 -smp cpus=1 \
          -m 1G \
          -drive file=$(pwd)/CentOS-8.4.2105-x86_64-boot.iso,index=1,media=cdrom \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -rtc base=localtime \
          -pidfile $(pwd)/centos.8.qcow2.pid \
          -display none \
          -vnc 0.0.0.0:1 \
          -daemonize
      ```
    * `-accel kvm` will use kvm as accelerator
    * `-cpu kvm64` specifies the cpu, use `qemu-system-x86_64 -cpu help` to see more options
    * `-drive ...` defines drives
        + adding `CentOS-8.4.2105-x86_64-boot.iso` as a cdrom
        + adding `centos.8.qcow2`(the virtual disk created) as a hard disk
    * `-rtc base=localtime` let the RTC start at local time
    * `-pidfile` specifies the pid file for the QEMU process
    * `-display none` will not display the video output but the guest will still see an emulated graphics card
    * `-vnc 0.0.0.0:1` will open a vnc port and redirect the VGA display over the VNC session
    * `-daemonize` will daemonize the QEMU process after initialization
4. use novnc to connect
    * ```shell
      docker run --name novnc --rm -p 6080:6080 \
              -e VNC_SERVER=host.docker.internal:5901 \
              --add-host host.docker.internal:host-gateway \
              -d wangz2019/docker-novnc:1.2.0
      ```
    * open http://localhost:6080/vnc.html with chrome, you will see the vnc client interface
5. [install centos 8 system](../linux/install.centos.8.by.boot.image.md)
    * after installation, login the machine with root and shutdown it
        + ```shell
          shutdown -h now
          ```
6. stop novnc docker container because we won't need it from now on
    * ```shell
      docker kill novnc
      ```
7. start qemu machine with the virtual disk
    * ```shell
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
    * `-nic user,hostfwd=tcp::1022-:22` will bind port `1022` in host with port `22` in guest
8. login and play with ssh
    * ```shell
      ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost
      ```
9. clean
    * stop virtual machine
        + ```shell
          ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost 'shutdown -h now'
          ```
    * you can also kill the process to force stop it
