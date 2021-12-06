## install with centos 8 or centos 7 (network required)

1. configure hosts for all nodes
    * assuming we have 5 nodes and mark `k8s-01` as the master while other nodes as the workers
    * ```shell
      cat >> /etc/hosts <<EOF
      192.168.123.11 k8s-01
      192.168.123.12 k8s-02
      192.168.123.13 k8s-03
      192.168.123.14 k8s-04
      192.168.123.15 k8s-05
      EOF
      ```
2. configure time and yum repos for all nodes
    * configure ntp
        + ```shell
          yum install -y chrony && systemctl enable chronyd && systemctl is-active chronyd \
              && chronyc sources && chronyc tracking \
              && timedatectl set-timezone 'Asia/Shanghai'
          ```
    * clean up all repos
        + ```shell
          rm -rf /etc/yum.repos.d/*
          ```
    * based on your os system, copy [all.in.one.8.repo](../resources/create.qemu.machine.for.kind/all.in.one.8.repo.md) as file `/etc/yum.repos.d/all.in.one.repo`
3. install base environment for all nodes
    * copy [setup.base.sh](../resources/setup.base.sh.md) as file `/tmp/setup.base.sh`
    * ```shell
      bash /tmp/setup.base.sh
      ```
4. install master
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
          mkdir -p $HOME/.kube
          cp /etc/kubernetes/admin.conf $HOME/.kube/config
          chown $(id -u):$(id -g) $HOME/.kube/config
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
          # TODO
          ```
5. install worker
    * ```shell
      # k8s-01 is master node
      $(ssh k8s-01 "kubeadm token create --print-join-command")
      ```
    * NOTE: no need to configure password less ssh login, just type your password once
    * wait for all pods in `kube-system` to be ready
        + ```shell
          kubectl -n kube-system wait --for=condition=ready pod --all
          ```
6. uninstall worker
    * find the workerNode which needs to remove
        + ```shell
          kubectl get node
          ```
    * in master node
        + ```shell
          kubectl drain $workerNode --ignore-daemonsets --delete-local-data
          kubectl delete node $workerNode
          ```
    * in the worker node which was removed from k8s cluster
        + ```shell
          kubeadm reset cleanup-node --force
          ```
7. uninstall master
    * ```shell
      # clean all the workers in cluster, then
      kubeadm reset cleanup-node --force
      ```
