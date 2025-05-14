# router

## pre-requirement

1. OS: Ubuntu 24.04.2 LTS
2. podman installed

## port 53 may be used by systemd-resolved

* check
    + ```shell
      PID=$(lsof -i :53 | awk 'NR==2 {print $2}')
      if [ -z "$PID" ]; then
        echo "Port 53 is not in use by any process."
      else
        echo "Port 53 is in use by process with PID: $PID"
        ps -p $PID
      fi
      ```
* if 53 is used by systemd-resolved, stop it
    + ```shell
      systemctl stop systemd-resolved
      systemctl disable systemd-resolved
      ```
    + ```shell
      cat > /etc/resolv.conf <<EOF
      nameserver 223.5.5.5
      nameserver 223.6.6.6
      EOF
      ```

```shell
export IMAGE=localhost/router:latest
bash container/build.sh
bash start.sh
```
