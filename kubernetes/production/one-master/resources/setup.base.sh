#! /bin/bash

set -e
set -x
# install docker
$(if [ "8" == "$(rpm --eval '%{centos_ver}')" ]; then echo dnf; else echo yum; fi) install -y device-mapper-persistent-data lvm2 docker-ce kubelet-1.23.3-0 kubeadm-1.23.3-0 kubectl-1.23.3-0 python3 iproute-tc \
    && sed -i -Ee 's#^(ExecStart=/usr/bin/dockerd .*$)#\1 --exec-opt native.cgroupdriver=systemd#g' /usr/lib/systemd/system/docker.service \
    && systemctl enable docker \
    && systemctl daemon-reload \
    && systemctl start docker
if type setenforce > /dev/null 2>&1; then
    selinuxenabled && setenforce 0
    sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config
fi
if [ -f "/etc/fstab" ]; then
    sed -i -Ee 's/^([^#].+ swap[ \t].*)/#\1/' /etc/fstab
fi
swapoff -a
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
