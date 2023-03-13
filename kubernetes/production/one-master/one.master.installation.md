# install master-only k8s cluster

## I. prepare environment

1. 1 node with centos 8 stream
    * aliyun.inner.geekcity.tech

## II. configure node

1. change hostname
    * ```shell
      hostnamectl set-hostname master.geekcity.tech
      ```
2. configure `/etc/hosts`
    * ```shell
      cat >> /etc/hosts <<EOF
      172.25.181.62 master.geekcity.tech
      EOF
      ```
3. configure repositories
    * remove all repo files
        + ```shell
          rm -rf /etc/yum.repos.d/*
          ```
    * copy [all.in.one.8.repo](resources/one-master/all.in.one.8.repo.md) as /etc/yum.repos.d/all.in.one.8.repo
4. configure ntp
    * ```shell
      dnf install -y chrony \
          && systemctl enable chronyd \
          && systemctl start chronyd \
          && chronyc sources \
          && chronyc tracking \
          && timedatectl set-timezone 'Asia/Shanghai'
      ```
5. stop and disable firewalld
    * ```shell
      systemctl stop firewalld && systemctl disable firewalld
      ```
6. install base environment
    * copy [setup.base.sh](resources/one-master/setup.base.sh.md) to /tmp/setup.base.sh
    * ```shell
      bash /tmp/setup.base.sh
      ```
7. prepare images for every node
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_calico_apiserver_v3.25.0.dim" \
          "docker.io_calico_cni_v3.25.0.dim" \
          "docker.io_calico_csi_v3.25.0.dim" \
          "docker.io_calico_ctl_v3.25.0.dim" \
          "docker.io_calico_kube-controllers_v3.25.0.dim" \
          "docker.io_calico_node-driver-registrar_v3.25.0.dim" \
          "docker.io_calico_node_v3.25.0.dim" \
          "docker.io_calico_pod2daemon-flexvol_v3.25.0.dim" \
          "docker.io_calico_typha_v3.25.0.dim" \
          "docker.io_registry.k8s.io_coredns_coredns_v1.9.3.dim" \
          "docker.io_registry.k8s.io_etcd_3.5.6-0.dim" \
          "docker.io_registry.k8s.io_kube-apiserver_v1.25.6.dim" \
          "docker.io_registry.k8s.io_kube-controller-manager_v1.25.6.dim" \
          "docker.io_registry.k8s.io_kube-proxy_v1.25.6.dim" \
          "docker.io_registry.k8s.io_kube-scheduler_v1.25.6.dim" \
          "docker.io_registry.k8s.io_pause_3.8.dim" \
          "docker.io_registry.k8s.io_pause_3.6.dim" \
          "quay.io_tigera_operator_v1.29.0.dim"
      do 
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE \
              && docker image load -i $IMAGE_FILE 
      done
     ```
8. install cri-docker
    * ```shell
      cp /root/data/cri-dockerd/0.3.1/cri-dockerd /usr/local/bin/cri-dockerd \
          && chmod u+x /usr/local/bin/cri-dockerd \
          && cp /root/data/cri-dockerd/cri-docker.service /etc/systemd/system/cri-docker.service \
          && cp /root/data/cri-dockerd/cri-docker.socket /etc/systemd/system/cri-docker.socket \
          && systemctl daemon-reload \
          && systemctl enable cri-docker.service \
          && systemctl enable --now cri-docker.socket \
          && systemctl status cri-docker.socket \
          && systemctl status docker
      ```

## III. install k8s

1. install master
    * initialize master
        + ```shell
          kubeadm init \
              --kubernetes-version=v1.25.6 \
              --pod-network-cidr=10.244.0.0/16 \
              --cri-socket unix:///var/run/cri-dockerd.sock \
              && systemctl restart kubelet
          ```
    * copy k8s config
        + ```shell
          mkdir -p $HOME/.kube \
              && cp /etc/kubernetes/admin.conf $HOME/.kube/config \
              && chown $(id -u):$(id -g) $HOME/.kube/config
          ```
    * download specific helm binary
        + ```shell
          cp /root/data/binary/helm/helm-v3.6.2-linux-amd64.tar.gz . \
              && tar zxvf helm-v3.6.2-linux-amd64.tar.gz linux-amd64/helm \
              && mkdir -p $HOME/bin \
              && mv linux-amd64/helm $HOME/bin/helm \
              && rm -rf linux-amd64/ helm-v3.6.2-linux-amd64.tar.gz
          ```
    * copy [tigera-operator.values.yaml](../resources/one-master/tigera-operator.values.yaml.md) as
      file `/tmp/tigera-operator.values.yaml`
    * install tigera-operator
        + ```shell
          helm install \
              --create-namespace --namespace calico-system \
              tigera-operator \
              /root/data/charts/tigera-operator-v3.25.0.tgz \
              --values /tmp/tigera-operator.values.yaml \
              --atomic
          ```
    * wait for all pods to be ready
        + ```shell
          kubectl -n calico-system wait --for=condition=ready pod --all
          kubectl -n kube-system wait --for=condition=ready pod --all
          kubectl wait --for=condition=ready node --all
          ```
2. remove master no schedule taint
    * ```shell
      kubectl taint nodes master.geekcity.tech node-role.kubernetes.io/control-plane:NoSchedule-
      ```