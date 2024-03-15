# fedora 39 change dns

## concepts

1. in fedora 39, `/etc/resolve.conf` is managed by systemd-resolved

## how

1. edit `/etc/systemd/resolved.conf`
    * ```shell
      sudo vi /etc/systemd/resolved.conf
      ```
    * add/modify `DNS=223.5.5.5`
2. restart systemd-resolved
    * ```shell
      sudo systemctl restart systemd-resolved
      ```
3. check
    * ```shell
      systemd-resolve --status
      ```
    * ```shell
      #nslookup www.google.com.hk
      nslookup www.baidu.com
      ```