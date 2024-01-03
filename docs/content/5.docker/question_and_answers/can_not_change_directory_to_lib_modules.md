### modprobe: can't change directory to '/lib/modules'

* logs
    + ```text
      time="2021-12-14T08:54:56.861178326Z" level=warning msg="Running modprobe bridge br_netfilter failed with message: ip: can't find device 'bridge'\nbridge                192512  1 br_netfilter\nstp                    16384  1 bridge\nllc                    16384  2 bridge,stp\nip: can't find device 'br_netfilter'\nbr_netfilter           24576  0 \nbridge                192512  1 br_netfilter\nmodprobe: can't change directory to '/lib/modules': No such file or directory\n, error: exit status 1"
      time="2021-12-14T08:54:56.862193050Z" level=warning msg="Running iptables --wait -t nat -L -n failed with message: `modprobe: can't change directory to '/lib/modules': No such file or directory\niptables v1.8.7 (legacy): can't initialize iptables table `nat': Table does not exist (do you need to insmod?)\nPerhaps iptables or your kernel needs to be upgraded.`, error: exit status 3"
      time="2021-12-14T08:54:56.897973928Z" level=info msg="stopping event stream following graceful shutdown" error="context canceled" module=libcontainerd namespace=moby
      time="2021-12-14T08:54:56.898042572Z" level=info msg="stopping healthcheck following graceful shutdown" module=libcontainerd
      time="2021-12-14T08:54:56.898090057Z" level=info msg="stopping event stream following graceful shutdown" error="context canceled" module=libcontainerd namespace=plugins.moby
      failed to start daemon: Error initializing network controller: error obtaining controller instance: failed to create NAT chain DOCKER: iptables failed: iptables -t nat -N DOCKER: modprobe: can't change directory to '/lib/modules': No such file or directory
      iptables v1.8.7 (legacy): can't initialize iptables table `nat': Table does not exist (do you need to insmod?)
      ```
* solution
    + ```shell
      modprobe ip_tables && echo 'ip_tables' >> /etc/modules-load.d/docker.conf
      ```
* reference
    + https://stackoverflow.com/questions/21983554/iptables-v1-4-14-cant-initialize-iptables-table-nat-table-does-not-exist-d
    + https://www.howtoforge.com/community/threads/iptables-table-does-not-exist-do-you-need-to-insmod.3196/