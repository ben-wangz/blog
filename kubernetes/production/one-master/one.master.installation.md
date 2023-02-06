1. 1 node with centos 8 stream
    * aliyun.inner.geekcity.tech
2. change hostname
    * ```shell
      hostnamectl set-hostname master.geekcity.tech
      ```
3. configure `/etc/hosts`
    * ```shell
      cat >> /etc/hosts <<EOF
      172.25.181.62 master.geekcity.tech
      EOF
      ```
4. configure repositories
    * remove all repo files
        + ```shell
          remove all repo configuration
          ```
    * copy [all.in.one.8.repo](resources/all.in.one.8.repo.md)
5. configure ntp
    * ```shell
      yum install -y chrony \
          && systemctl enable chronyd \
          && systemctl start chronyd \
          && chronyc sources \
          && chronyc tracking \
          && timedatectl set-timezone 'Asia/Shanghai'
      ```
6. stop and disable firewalld
    * ```shell
      systemctl stop firewalld && systemctl disable firewalld
      ```
7. install base environment
    * copy [setup.base.sh](resources/setup.base.sh.md) to /tmp/setup.base.sh
    * ```shell
      bash /tmp/setup.base.sh
      ```
8. prepare images for every node
    * ```shell
      DOCKER_IMAGE_PATH=/root/docker-images && mkdir -p $DOCKER_IMAGE_PATH
      BASE_URL="https://resource-ops.lab.zjvis.net:32443/docker-images"
      for IMAGE in "docker.io_calico_apiserver_v3.21.2.dim" \
          "docker.io_calico_pod2daemon-flexvol_v3.21.2.dim" \
          "docker.io_calico_cni_v3.21.2.dim" \
          "docker.io_calico_typha_v3.21.2.dim" \
          "docker.io_calico_kube-controllers_v3.21.2.dim" \
          "docker.io_calico_node_v3.21.2.dim" \
          "docker.io_k8s.gcr.io_kube-apiserver_v1.23.3.dim" \
          "docker.io_k8s.gcr.io_pause_3.6.dim" \
          "docker.io_k8s.gcr.io_kube-controller-manager_v1.23.3.dim" \
          "docker.io_k8s.gcr.io_coredns_coredns_v1.8.6.dim" \
          "docker.io_k8s.gcr.io_kube-proxy_v1.23.3.dim" \
          "docker.io_k8s.gcr.io_etcd_3.5.1-0.dim" \
          "docker.io_k8s.gcr.io_kube-scheduler_v1.23.3.dim" \
          "docker.io_registry_2.7.1.dim" \
          "quay.io_tigera_operator_v1.23.3.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          if [ ! -f $IMAGE_FILE ]; then
              TMP_FILE=$IMAGE_FILE.tmp \
              && curl -o "$TMP_FILE" -L "$BASE_URL/$IMAGE" \
              && mv $TMP_FILE $IMAGE_FILE
          fi
          docker image load -i $IMAGE_FILE && rm -f $IMAGE_FILE
      done
      ```
9. install master
    * initialize master
        + ```shell
          # TODO check kubeadm version to set --kubernetes-version
          kubeadm init --kubernetes-version=v1.22.1 --pod-network-cidr=172.21.0.0/20 --image-repository registry.aliyuncs.com/google_containers
          # TODO remove
          sed -i -Ee "s/^([^#].*--port=0.*)/#\1/g" /etc/kubernetes/manifests/kube-scheduler.yaml
          sed -i -Ee "s/^([^#].*--port=0.*)/#\1/g" /etc/kubernetes/manifests/kube-controller-manager.yaml
          systemctl restart kubelet
          ```
    * copy k8s config
        + ```shell
          mkdir -p $HOME/.kube \
              && cp /etc/kubernetes/admin.conf $HOME/.kube/config \
              && chown $(id -u):$(id -g) $HOME/.kube/config
          ```
    * copy [calico.yaml](../resources/calico.yaml.md) as file `/tmp/calico.yaml` and apply it to k8s cluster
        + ```shell
          kubectl apply -f /tmp/calico.yaml
          ```
    * wait for all pods in `kube-system` to be ready
        + ```shell
          kubectl -n kube-system wait --for=condition=ready pod --all
          ```
    * download specific helm binary
        + ```shell
          # mirror of https://get.helm.sh
          BASE_URL=https://resource-ops.lab.zjvis.net:32443/binary/helm \
              && curl -LO ${BASE_URL}/helm-v3.6.2-linux-amd64.tar.gz \
              && tar zxvf helm-v3.6.2-linux-amd64.tar.gz linux-amd64/helm \
              && mkdir -p $HOME/bin \
              && mv linux-amd64/helm $HOME/bin/helm \
              && rm -rf linux-amd64/ helm-v3.6.2-linux-amd64.tar.gz

          ```