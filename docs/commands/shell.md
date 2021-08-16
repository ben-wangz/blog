### clean files 3 days ago

```shell
find /root/database/backup/db.sql.*.gz -mtime +3 -exec rm {} \;
```

### ssh without affect $HOME/.ssh/known_hosts

```shell
ssh -o "UserKnownHostsFile /dev/null" root@aliyun.geekcity.tech
```

### rsync file to remote

```shell
rsync -av --delete \
    -e 'ssh -o "UserKnownHostsFile /dev/null" -p 22' \
    --exclude build/ \
    $HOME/git_projects/blog root@aliyun.geekcity.tech:/root/develop/blog
```

### looking for network connections

* all connections
    + ```shell
      lsof -i -P -n
      ```
* specific port
    + ```shell
      lsof -i:8083
      ```

### sync clock

```shell
yum -y install ntp ntpdate \
        && ntpdate ntp.aliyun.com \
        && hwclock --systohc \
        && hwclock -w
```

### settings for screen

```shell
cat > $HOME/.screenrc <<EOF
startup_message off
caption always "%{.bW}%-w%{.rW}%n %t%{-}%+w %=%H %Y/%m/%d "
escape ^Jj #Instead of control-a

shell -$SHELL
EOF
```
