# loop back device with lvm

## main usage

1. virtualize a file as an image disk and added it into lvm

## conceptions

1. loop back device
2. lvm

## practise

### pre-requirements

* you need to know basic [conceptions](#conceptions)
* [logical volume manager](logical.volume.manager.md) has been read and practised

### purpose

* virtualize a file as an image disk
* add image disk to lvm
* create LV to use the storage

### do it

1. prepare a virtual machine according to [create centos 8 with qemu](../qemu/create.centos.8.with.qemu.md)
2. create an image file
    * ```shell
      dd if=/dev/zero of=disk.img bs=1M count=1024
      ```
3. bind with loop back device
    * ```shell
      losetup /dev/loop0 disk.img
      ```
4. create VG with loop back device
    * ```shell
      vgcreate vgtest /dev/loop0
      ```
5. create LV
    * ```shell
      lvcreate -L 100M -n data vgtest
      ```
6. create file system on LV
    * ```shell
      mkfs.xfs /dev/mapper/vgtest-data
      ```
7. mount and test
    * ```shell
      mount /dev/mapper/vgtest-data /mnt
      touch /mnt/test-file
      echo this is a part of testing > /mnt/test-file
      echo this is a another part of testing >> /mnt/test-file
      cat /mnt/test-file
      ```