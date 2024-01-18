# single-master

## references

* https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/

## prepare

1. 1 node with fedora 38
2. install necessary packages for each node
    * ```shell
      dnf install iptables iproute-tc cri-o containernetworking-plugins \
          kubernetes-client kubernetes-node kubernetes-kubeadm
      ```

## configure node

1. change hostname of master node
    * ```shell
      hostnamectl set-hostname master.geekcity.tech
      ```
2. configure `/etc/hosts`
    * ```shell
      cat >> /etc/hosts <<EOF
      172.25.181.62 master.geekcity.tech
      EOF
      ```
3. configure ntp
    * ```shell
      dnf install -y chrony \
          && systemctl enable chronyd \
          && systemctl start chronyd \
          && chronyc sources \
          && chronyc tracking \
          && timedatectl set-timezone 'Asia/Shanghai'
      ```
4. turn off selinux, firewalld and swap of each node
    * ```shell
      <!-- @include: @src/articles/commands/linux/turn-off-selinux.sh -->
      <!-- @include: @src/articles/commands/linux/turn-off-firewalld.sh -->
      <!-- @include: @src/articles/commands/linux/turn-off-swap.sh -->
      ```
5. configure forwarding IPv4
    * ```shell
      cat <<EOF | tee /etc/modules-load.d/k8s.conf
      overlay
      br_netfilter
      EOF

      modprobe overlay
      modprobe br_netfilter

      # sysctl params required by setup, params persist across reboots
      cat <<EOF | tee /etc/sysctl.d/k8s.conf
      net.bridge.bridge-nf-call-iptables  = 1
      net.bridge.bridge-nf-call-ip6tables = 1
      net.ipv4.ip_forward                 = 1
      EOF

      # Apply sysctl params without reboot
      sysctl --system

      # verify
      lsmod | grep br_netfilter
      lsmod | grep overlay
      sysctl net.bridge.bridge-nf-call-iptables net.bridge.bridge-nf-call-ip6tables net.ipv4.ip_forward
      ```
6. enable cri-o
    * ```shell
      systemctl enable --now crio
      ```