# shell

## clean files 3 days ago

```shell
find /root/database/backup/db.sql.*.gz -mtime +3 -exec rm {} \;
```

## ssh without affect $HOME/.ssh/known_hosts

```shell
ssh -o "UserKnownHostsFile /dev/null" root@aliyun.geekcity.tech
ssh -o "UserKnownHostsFile /dev/null" -o "StrictHostKeyChecking=no" root@aliyun.geekcity.tech
```

## rsync file to remote

```shell
rsync -av --delete \
    -e 'ssh -o "UserKnownHostsFile /dev/null" -p 22' \
    --exclude build/ \
    $HOME/git_projects/blog root@aliyun.geekcity.tech:/root/develop/blog
```

## looking for network connections

* all connections
    + ```shell
      lsof -i -P -n
      ```
* specific port
    + ```shell
      lsof -i:8083
      ```

## port forwarding with ssh tunnel

* local port forwarding
    + ```shell
      ssh -L [local_port]:[remote_host]:[remote_port] [user]@[gateway] -N -f
      ```

## sync clock

```shell
yum install -y chrony \
    && systemctl enable chronyd \
    && (systemctl is-active chronyd || systemctl start chronyd) \
    && chronyc sources \
    && chronyc tracking \
    && timedatectl set-timezone 'Asia/Shanghai'
```

## settings for screen

```shell
cat > $HOME/.screenrc <<EOF
startup_message off
caption always "%{.bW}%-w%{.rW}%n %t%{-}%+w %=%H %Y/%m/%d "
escape ^Kk #Instead of control-a

shell -$SHELL
EOF
```

## count code lines

```shell
find . -name "*.java" | xargs cat | grep -v ^$ | wc -l
git ls-files | while read f; do git blame --line-porcelain $f | grep '^author '; done | sort -f | uniq -ic | sort -n
git log --author="ben.wangz" --pretty=tformat: --numstat | awk '{ add += $1; subs += $2; loc += $1 - $2 } END { printf "added lines: %s removed lines: %s total lines: %s\n", add, subs, loc }' -
```

## check sha256

```shell
echo "1984c349d5d6b74279402325b6985587d1d32c01695f2946819ce25b638baa0e *ubuntu-20.04.3-preinstalled-server-armhf+raspi.img.xz" | shasum -a 256 --check
```

## check command existence

```shell
if type firewall-cmd > /dev/null 2>&1; then 
    firewall-cmd --permanent --add-port=8080/tcp; 
fi
```

## set hostname

```shell
hostnamectl set-hostname develop
```

## add remote key

```shell
ssh -o "UserKnownHostsFile /dev/null" \
    root@aliyun.geekcity.tech \
    "mkdir -p /root/.ssh && chmod 700 /root/.ssh && echo '$SOME_PUBLIC_KEY' \
    >> /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys"
```

## check service logs with journalctl

```shell
journalctl -u docker
```

## script path

```shell
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
```

## query for ip address
```shell
# dnf -y install curl jq
curl -sL 'https://www.ip.cn/api/index?ip&type=0' | jq -r '.ip'
```

## extend fedora root partition
1. example
    * ```text
      [root@cloud-template ~]# fdisk -l
      Disk /dev/sda: 64 GiB, 68719476736 bytes, 134217728 sectors
      Disk model: QEMU HARDDISK
      Units: sectors of 1 * 512 = 512 bytes
      Sector size (logical/physical): 512 bytes / 512 bytes
      I/O size (minimum/optimal): 512 bytes / 512 bytes
      Disklabel type: gpt
      Disk identifier: D90CD1A6-E818-447F-909B-8EE0135EB122
      
      Device       Start       End   Sectors Size Type
      /dev/sda1     2048      4095      2048   1M BIOS boot
      /dev/sda2     4096   2101247   2097152   1G Linux filesystem
      /dev/sda3  2101248 134215679 132114432  63G Linux LVM
      
      
      Disk /dev/mapper/fedora-root: 15 GiB, 16106127360 bytes, 31457280 sectors
      Units: sectors of 1 * 512 = 512 bytes
      Sector size (logical/physical): 512 bytes / 512 bytes
      I/O size (minimum/optimal): 512 bytes / 512 bytes
      
      
      Disk /dev/zram0: 3.81 GiB, 4090494976 bytes, 998656 sectors
      Units: sectors of 1 * 4096 = 4096 bytes
      Sector size (logical/physical): 4096 bytes / 4096 bytes
      I/O size (minimum/optimal): 4096 bytes / 4096 bytes

      [root@cloud-template ~]# lsblk -f
      NAME            FSTYPE      FSVER    LABEL UUID                                   FSAVAIL FSUSE% MOUNTPOINTS
      sda
      ├─sda1
      ├─sda2          xfs                        65396769-d1e4-4807-862a-9db0a68ef0cf    735.4M    23% /boot
      └─sda3          LVM2_member LVM2 001       ddhEGK-SuWg-Xl6y-MhIJ-Ip7e-2FGy-4PqeVg
        └─fedora-root xfs                        cccf36b1-980d-4132-849a-9e02439bc11b     13.2G    11% /
      zram0

      [root@cloud-template ~]# pvs
        PV         VG     Fmt  Attr PSize   PFree
        /dev/sda3  fedora lvm2 a--  <63.00g <48.00g
      [root@cloud-template ~]# lvs
        LV   VG     Attr       LSize  Pool Origin Data%  Meta%  Move Log Cpy%Sync Convert
        root fedora -wi-ao---- 15.00g
      ```
2. extend lv and xfs
    * ```shell
      lvextend /dev/mapper/fedora-root -l+100%FREE
      xfs_growfs /dev/mapper/fedora-root
      ```

## generate random string

```shell
tr -dc A-Za-z0-9 </dev/urandom | head -c 16
```

## parquet-tools

* references: https://github.com/NathanHowell/parquet-tools
* installation
    + ```shell
      python3 -m pip install parquet-tools
      ```
* operations
    + ```shell
      parquet-tools:master show /tmp/file.parquet
      ```