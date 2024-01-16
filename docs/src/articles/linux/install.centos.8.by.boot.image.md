# install centos 8 by boot image

## purpose

* install centos 8

## pre-requirements

* a machine with x86_64 cpu
* you can use qemu to virtualize a machine
    + [install with linux](../qemu/install.with.linux)
    + [install with mac](../qemu/install.with.mac)

## do it

1. choose installation language: "English" -> "English(United States)"
    + ![choose installation language](images/choose.installation.language.png)
    + ![installation summary origin](images/installation.summary.origin.png)
2. keep Keyboard as "English(US)"
3. configure network and hostname
    + turn network on
    + set hostname to "node-01"
    + ![configure network and hostname](images/configure.network.and.hostname.png)
4. set time&date:
    + turn network time on
    + open configuration page and add "ntp.aliyun.com" as ntp servers if origin one cannot connect to
    + set "Region" to "Asia" and "City" to "Shanghai"
    + ![set date and time](images/set.date.and.time.png)
5. keep installation source not changed(will be updated after network connected)
6. choose local disk as "Installation Destination"
    + ![choose installation destination](images/choose.installation.destination.png)
7. in "Software Selection"
    + choose "Minimal Install"
    + do not add any additional software
    + ![software selection](images/software.selection.png)
8. set root password
    + ![set root password](images/set.root.password.png)
9. settings finished state: confirm and click "Begin Installation"
    + ![installation summary finished](images/installation.summary.finished.png)
10. waiting for installation to be finished
    + just click "Reboot System"
    + ![waiting for installation finished](images/waiting.for.installation.finished.png)
11. login and check
