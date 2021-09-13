# develop with kubernetes

## install with centos 8 or centos 7

1. configure hosts with all nodes
    * ```shell
      cat >> /etc/hosts <<EOF
      192.168.123.11 k8s-01
      192.168.123.12 k8s-02
      192.168.123.13 k8s-03
      192.168.123.14 k8s-04
      192.168.123.15 k8s-05
      EOF
      ```
2. configure yum repos
    * clean up all repos
    * ```shell
      rm -rf /etc/yum.repos.d/*
      ```
    * paste content as file `/etc/yum.repos.d/all.in.one.repo`
    * for centos 8
        + ```text
          [base]
          name=CentOS-$releasever - Base - mirrors.aliyun.com
          baseurl=https://mirrors.aliyun.com/centos/$releasever/BaseOS/$basearch/os/
          gpgcheck=1
          gpgkey=https://mirrors.aliyun.com/centos/RPM-GPG-KEY-CentOS-Official

          [extras]
          name=CentOS-$releasever - Extras - mirrors.aliyun.com
          baseurl=https://mirrors.aliyun.com/centos/$releasever/extras/$basearch/os/
          gpgcheck=1
          gpgkey=https://mirrors.aliyun.com/centos/RPM-GPG-KEY-CentOS-Official

          [AppStream]
          name=CentOS-$releasever - AppStream - mirrors.aliyun.com
          failovermethod=priority
          baseurl=https://mirrors.aliyun.com/centos/$releasever/AppStream/$basearch/os/
          gpgcheck=1
          gpgkey=https://mirrors.aliyun.com/centos/RPM-GPG-KEY-CentOS-Official

          [docker-ce-stable]
          name=Docker CE Stable - $basearch
          baseurl=http://mirrors.aliyun.com/docker-ce/linux/centos/$releasever/$basearch/stable
          enabled=1
          gpgcheck=1
          gpgkey=http://mirrors.aliyun.com/docker-ce/linux/centos/gpg

          [kubernetes]
          name=Kubernetes
          baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-$basearch/
          enabled=1
          gpgcheck=0
          ```
    * for centos 7
        + ```text
          [base]
          name=CentOS-$releasever
          enabled=1
          failovermethod=priority
          baseurl=http://mirrors.aliyun.com/centos/$releasever/os/$basearch/
          gpgcheck=0

          [updates]
          name=CentOS-$releasever
          enabled=1
          failovermethod=priority
          baseurl=http://mirrors.aliyun.com/centos/$releasever/updates/$basearch/
          gpgcheck=0

          [extras]
          name=CentOS-$releasever
          enabled=1
          failovermethod=priority
          baseurl=http://mirrors.aliyun.com/centos/$releasever/extras/$basearch/
          gpgcheck=0

          [docker-ce-stable]
          name=Docker CE Stable - $basearch
          baseurl=http://mirrors.aliyun.com/docker-ce/linux/centos/$releasever/$basearch/stable
          enabled=1
          gpgcheck=0

          [kubernetes]
          name=Kubernetes
          baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el$releasever-$basearch/
          enabled=1
          gpgcheck=0
          ```
3. install base environment at all nodes in cluster
    * paste content as file `/tmp/setup.sh`
    * ```shell
      #! /bin/bash
      
      set -e
      set -x
      # install docker
      $(if [ "8" == "$(rpm --eval '%{centos_ver}')" ]; then echo dnf; else echo yum; fi) install -y device-mapper-persistent-data lvm2 docker-ce kubelet kubeadm kubectl python3 iproute-tc \
          && sed -i -Ee 's#^(ExecStart=/usr/bin/dockerd .*$)#\1 --exec-opt native.cgroupdriver=systemd#g' /usr/lib/systemd/system/docker.service \
          && systemctl enable docker \
          && systemctl daemon-reload \
          && systemctl start docker
      if type setenforce > /dev/null 2>&1; then 
          selinuxenabled && setenforce 0
          sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
      fi
      if [ -f "/etc/fstab" ]; then
          sed -i -Ee 's/^([^#][^ \t]+ swap[ \t]+ swap[ \t].*)/#\1/' /etc/fstab
      fi
      swapoff -a
      if type firewall-cmd > /dev/null 2>&1 && systemctl is-active firewalld; then
          # Calico networking (BGP)
          firewall-cmd --permanent --add-port=179/tcp
          # etcd datastore
          firewall-cmd --permanent --add-port=2379-2380/tcp
          # Default cAdvisor port used to query container metrics
          firewall-cmd --permanent --add-port=4149/tcp
          # Calico networking with VXLAN enabled
          firewall-cmd --permanent --add-port=4789/udp
          # Calico networking with Typha enabled
          firewall-cmd --permanent --add-port=5473/tcp
          # Kubernetes API port
          firewall-cmd --permanent --add-port=6443/tcp
          # Health check server for Calico (if using Calico/Canal)
          firewall-cmd --permanent --add-port=9099/tcp
          # API which allows full node access
          firewall-cmd --permanent --add-port=10250/tcp
          firewall-cmd --permanent --add-port=10251/tcp
          firewall-cmd --permanent --add-port=10252/tcp
          # Unauthenticated read-only port, allowing access to node state
          firewall-cmd --permanent --add-port=10255/tcp
          # Health check server for Kube Proxy
          firewall-cmd --permanent --add-port=10256/tcp
          # Default port range for external service ports
          # Typically, these ports would need to be exposed to external load-balancers,
          #     or other external consumers of the application itself
          firewall-cmd --permanent --add-port=30000-32767/tcp
          firewall-cmd --add-masquerade --permanent
          firewall-cmd --reload
      fi
      cat > /etc/modules-load.d/k8s.conf <<EOF
      br_netfilter
      EOF
      cat > /etc/sysctl.d/k8s.conf <<EOF
      net.bridge.bridge-nf-call-ip6tables = 1
      net.bridge.bridge-nf-call-iptables = 1
      EOF
      sysctl --system
      # install 
      systemctl enable kubelet
      systemctl start kubelet
      docker pull registry.aliyuncs.com/google_containers/coredns:1.8.4
      docker tag registry.aliyuncs.com/google_containers/coredns:1.8.4 registry.aliyuncs.com/google_containers/coredns:v1.8.4
      ```
    * install base environment with `/tmp/setup.sh`
    * ```shell
      bash /tmp/setup.sh
      ```
4. install master
    * initialize master
    * ```shell
      kubeadm init --kubernetes-version=v1.22.1 --pod-network-cidr=172.21.0.0/20 --image-repository registry.aliyuncs.com/google_containers
      # TODO remove
      sed -i -Ee "s/^([^#].*--port=0.*)/#\1/g" /etc/kubernetes/manifests/kube-scheduler.yaml
      sed -i -Ee "s/^([^#].*--port=0.*)/#\1/g" /etc/kubernetes/manifests/kube-controller-manager.yaml
      systemctl restart kubelet
      ```
    * copy k8s config
    * ```shell
      mkdir -p $HOME/.kube
      cp /etc/kubernetes/admin.conf $HOME/.kube/config
      chown $(id -u):$(id -g) $HOME/.kube/config
      ```
    * ```shell
      # apply calico
      ```
    * wait for all pods in `kube-system` to be ready
    * ```shell
      kubectl -n kube-system wait --for=condition=ready pod --all
      ```
    * download specific helm binary
    * ```shell
      // TODO
      ```
5. install worker
    * ```shell
      # k8s-01 is master node
      $(ssh k8s-01 'kubeadm token create --print-join-command')
      ```
    * NOTE: no need to configure password less ssh login, just type your password once

## install with centos 8 offline
