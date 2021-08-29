# develop with qemu

## I. installation

### mac os

```shell
brew install qemu
```

### linux(centos 8 for example)

```shell
dnf install qemu-kvm
```

### windows(windows 10 for example)

* download from [binary](https://qemu.weilnetz.de/w64/)
* double click to run it

## II. build images

### build linux amd64 (CentOS-8.3.2011-x86_64 for example)

* download image
    + ```shell
      curl -LO https://mirrors.aliyun.com/centos/8.3.2011/isos/x86_64/CentOS-8.3.2011-x86_64-minimal.iso
      ```
* crate virtual disk image
    + ```shell
      qemu-img create -f qcow2 centos.8.qcow2 40G
      ```
    + qcow2 is a type of image format: [Qcow2 at linux-kvm.org](https://www.linux-kvm.org/page/Qcow2)
    + more formats supported by qemu: [qemu system images](https://qemu-project.gitlab.io/qemu/system/images.html)
    + [manual for qemu-img](https://qemu-project.gitlab.io/qemu/tools/qemu-img.html)
* start qemu to install system
    + ```shell
      qemu-system-x86_64 -cpu kvm64 -smp cpus=2 \
          -m 2G \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -drive file=$(pwd)/CentOS-8.3.2011-x86_64-minimal.iso,index=1,media=cdrom \
          -rtc base=localtime
      ```
    + [helper for qemu-xxx](https://qemu-project.gitlab.io/qemu/system/invocation.html#hxtool-0)
* centos.8.qcow2 can be ported to any other system and start with qemu
    + ```shell
      qemu-system-x86_64 -cpu kvm64 -smp cpus=2 \
          -m 1G \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -rtc base=localtime \
          -pidfile $(pwd)/centos.8.qcow2.pid
      ```
    + by default, this command will start the QEMU monitor in a window
    + use `-curses` to enable QEMU to display the VGA output when in text mode using a curses/ncurses interface
    + use `-nographic` to disable graphical output so that QEMU is a simple command line application
    + use `-display none` to not display video output("guest will still see an emulated graphics card")
    + connect with sshd
        * usually combined with `-display none`
        * use `-nic user,hostfwd=tcp::1022-:22` to bind host port(1022) with the port in virtual machine(22)
* use sshd to connect
    + ```shell
      qemu-system-x86_64 -cpu kvm64 -smp cpus=2 \
          -m 2G \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -rtc base=localtime \
          -pidfile $(pwd)/centos.8.qcow2.pid \
          -nic user,hostfwd=tcp::1022-:22 \
          -display none
      ```
    + you may also combined with `nohup` or `screen` to make it run at background
    + ```shell
      ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost
      ```
* use vnc to connect
    + ```shell
      qemu-system-x86_64 -cpu kvm64 -smp cpus=2 \
          -m 2G \
          -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
          -rtc base=localtime \
          -pidfile $(pwd)/centos.8.qcow2.pid \
          -nic user,hostfwd=tcp::1022-:22 \
          -display none \
          -vnc 0.0.0.0:1
      ```
    + use vnc client to connect to it
        * such as novnc in docker
        * ```shell
          docker run --rm -p 6081:6081 wangz2019/novnc:1.0.0
          ```
    + it's okay to combine vnc with ssh

### build linux arm64 (CentOS-8.3.2011-aarch64 for example)

* ```shell
  curl -LO https://mirrors.aliyun.com/centos/8.3.2011/isos/aarch64/CentOS-8.3.2011-aarch64-minimal.iso
  ```

### build windows image (win10)

* ```shell
  curl -LO https://down.fjweite.cn/syj/windows_10_professional_x64_2021.iso
  ```

## III. use it as a developing machine

* purpose: compile or run programs in specified environment
