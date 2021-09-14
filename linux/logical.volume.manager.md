# logical volume manager (LVM)

## main usage

1. physical to logical abstraction
    * you can manage multiple disks as one logical volume
    * you can split one disk into multiple logical volumes
2. dynamic volume resizing
    * resizing logical disks
3. add more physical disks or move old physical disks for needs
    * in combination with hot swapping, you can add or replace disks without stopping the service

## conceptions

![lvm.svg from wikipedia](resources/lvm.svg)

1. PVs: physical volumes
    * can be hard disks or partitions
    * LVM treats each PV as a sequences of PEs(physical extends)
2. PEs: physical extends
    * PEs have a uniform size, which is the minimal storage size for LVM
3. LEs: logical extends
    * usually mapping to PEs as one to one relationship
4. VG: volume group
    * each VG is a pool of LE
    * consequently, VG consists of multiple PVs
5. LVs: logical volumes
    * LEs can be concatenated together into virtual disk partitions called LVs
    * LVs can be used as raw block devices, which is supported by Linux kernel
    * in result, we can use LVs as file systems, swap and so on

## practise

### pre-requirements in this practise

* you need to know basic [conceptions](#conceptions)
* but only PV, PE, VG and LV appear in commands

### purpose

* check volume structures after default centos 8 installation
* resize LV
    + ext filesystem
    + xfs filesystem
    + swap
* resize PV
    + will affect VG size
    + won't affect disk partition size
* create PV? VG?

### do it

1. prepare a virtual machine according to [create centos 8 with qemu](../qemu/create.centos.8.with.qemu.md)
2. check volume structures
3. resize LV of ext filesystem
4. resize LV of xfs filesystem
5. resize LV of swap
6. resize PV
