# single-master

## references

* https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/
* https://docs.fedoraproject.org/en-US/quick-docs/using-kubernetes/

## prepare

1. 1 node with fedora 38(use fedora 39 if you are play with aliyun ecs)
2. root account required
3. install necessary packages for each node
    * ```shell
      dnf -y install iptables iproute-tc
      ```
    * ```shell
      KUBE_VERSION=1.27
      dnf -y module enable cri-o:${KUBE_VERSION}
      dnf -y install cri-o
      ```
    * ```shell
      KUBE_VERSION=1.27
      cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
      [kubernetes]
      name=Kubernetes
      baseurl=https://pkgs.k8s.io/core:/stable:/v${KUBE_VERSION}/rpm/
      enabled=1
      gpgcheck=1
      gpgkey=https://pkgs.k8s.io/core:/stable:/v${KUBE_VERSION}/rpm/repodata/repomd.xml.key
      exclude=kubelet kubeadm kubectl cri-tools kubernetes-cni
      EOF
      dnf install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
      ```
4. (for aliyun) modify grub and reboot
    * ```shell
      echo 'GRUB_CMDLINE_LINUX="cgroup_enable=cpu"' >> /etc/default/grub
      grub2-mkconfig -o /boot/grub2/grub.cfg
      reboot
      ```

## configure node

1. change hostname of master node
    * ```shell
      hostnamectl set-hostname k8s-master
      ```
2. configure `/etc/hosts`
    * ```shell
      cat >> /etc/hosts <<EOF
      172.25.181.62 k8s-master
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
      <!-- @include: @src/articles/commands/linux/turn-off-swap-fedora.sh -->
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
          # If you use custom podCIDR (not 10.244.0.0/16) you first need to download the above manifest and modify the network to match your one.
          kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
          ```
    * [flannel by helm chart](../../helm/flannel/README.md)
    * [calico by helm chart](../../helm/calico/README.md)

## test with deployment

1. prepare `nginx-deployment.yaml`
    * ```yaml
      <!-- @include: nginx-deployment.yaml -->
      ```
2. apply to cluster
    * ```shell
      kubectl apply -f nginx-deployment.yaml
      ```
3. check pods
    * ```shell
      kubectl get pod
      ```

## troubles
1. "cni0" already has an IP address different from 10.2.44.1/24
    * https://github.com/kubernetes/kubernetes/issues/39557#issuecomment-457839765
    * ```shell
      ip link delete cni0
      ```

## uninstallation
1. uninstall by kubeadm
    * ```shell
      kubeadm reset
      ```

## addtional software
1. [install argocd by helm](../../helm/argocd/README.md)
2. [install ingress by argocd](../../argocd/ingress/README.md)
3. [install cert-manager by argocd](../../argocd/cert-manager/README.md)
