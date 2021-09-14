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
    sed -i -Ee 's/^([^#].+ swap[ \t].*)/#\1/' /etc/fstab
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