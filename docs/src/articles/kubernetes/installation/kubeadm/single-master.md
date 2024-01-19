# single-master

## references

* https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/
* https://docs.fedoraproject.org/en-US/quick-docs/using-kubernetes/

## prepare

1. 1 node with fedora 38
2. root account required
3. install necessary packages for each node
    * ```shell
      dnf install -y iptables iproute-tc cri-o containernetworking-plugins \
          kubernetes-client kubernetes-node kubernetes-kubeadm
      ```

## configure node

1. change hostname of master node
    * ```shell
      hostnamectl set-hostname master
      ```
2. configure `/etc/hosts`
    * ```shell
      cat >> /etc/hosts <<EOF
      172.25.181.62 master
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
7. enable kubelet
    * ```shell
      systemctl enable --now kubelet
      ```
8. prepare `kubeadm.conf.yaml`
    * ```yaml
      <!-- @include: @src/articles/kubernetes/installation/kubeadm/kubeadm.conf.yaml -->
      ```
9. initialize the cluster
    * ::: code-tabs#shell
      @tab with-image-mirror
      ```shell
      sed -i 's/imageRepository: .*/imageRepository: m.daocloud.io\/registry.k8s.io/g' kubeadm.conf.yaml
      kubeadm init --config kubeadm.conf.yaml
      ```
      @tab without-image-mirror
      ```shell
      kubeadm init --config kubeadm.conf.yaml
      ```
      :::
10. copy kubeconfig to local
    * ```shell
      <!-- @include: @src/articles/kubernetes/installation/copy-kubeconfig.sh -->
      ```
11. allow control plane node to run pods
    * ```shell
      kubectl taint nodes --all node-role.kubernetes.io/control-plane-
      ```
12. install pod network(chose one of the methods below)
    * flannel by kubectl
        + ```shell
          kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
          ```
    * [flannel by helm chart](../../helm/flannel/README.md)
    * [calico by helm chart](../../helm/calico/README.md)

## uninstallation
1. uninstall by kubeadm
    * ```shell
      kubeadm reset
      ```

## addtional software
1. [install argocd by helm](../../helm/argocd/README.md)
2. [install ingress by argocd](../../argocd/ingress/README.md)
3. [install cert-manager by argocd](../../argocd/cert-manager/README.md)
