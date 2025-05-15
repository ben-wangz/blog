# K3s

## Introduction
K3s is a lightweight, fully compliant Kubernetes distribution that is easy to install and manage. This guide provides step-by-step instructions for installing a K3s cluster, managing nodes, and uninstalling K3s.

## Prerequisites
* A Linux-based system with internet access.
* Root or sudo privileges.
* hardware resource requirement can be found [here](https://docs.k3s.io/zh/installation/requirements#cpu-and-memory)

## configure hostname and hosts

* assuming we have 4 nodes and configured with hostnames: `k3s1`, `k3s2`, `k3s3` and `k3s4`
    * ```shell
      hostnamectl set-hostname <hostname>
      ```
* add following lines to `/etc/hosts` on each node
    * change ip according to your own environment
    * ```text
      192.168.1.104 k3s1
      192.168.1.105 k3s2
      192.168.1.106 k3s3
      192.168.1.107 k3s4
      ```

## Installing K3s Cluster

0. configure `/etc/rancher/k3s/registries.yaml` on each node
    * ```yaml
      mirrors:
        docker.io:
          endpoint:
            - "https://dockerproxy.net"
      ```

1. initialize first control panel
    * ```shell
      curl -sfL https://rancher-mirror.rancher.cn/k3s/k3s-install.sh | INSTALL_K3S_MIRROR=cn sh -s - server --cluster-init --flannel-backend=vxlan --node-taint "node-role.kubernetes.io/control-plane=true:NoSchedule"
      ```
2. get join-token from control panel
    * ```shell
      cat /var/lib/rancher/k3s/server/node-token
      ```
3. initialize workers(agent in k3s) and join to control panel with the token
    * ```shell
      curl -sfL https://rancher-mirror.rancher.cn/k3s/k3s-install.sh | INSTALL_K3S_MIRROR=cn K3S_URL=https://k3s1:6443 K3S_TOKEN=<join-token> sh -
      ```
## Copy configuration

* ```shell
  mkdir -p $HOME/.kube
  cp /etc/rancher/k3s/k3s.yaml $HOME/.kube/config
  ```

## Uninstalling K3s Cluster
1. remove worker(k3s-agent) from cluster
    * cordon a node: prevent new pods from being scheduled to the node
        + ```shell
          kubectl cordon <node-name>
          ```
    * drain a node: move all pods to other nodes
        + ```shell
          kubectl drain <node-name> --ignore-daemonsets --delete-emptydir-data
          # check if all pods are moved to other nodes
          #kubectl get pods -o wide --all-namespaces | grep <node-name>
          ```
    * delete the node
        + ```shell
          kubectl delete node <node-name>
          ```
2. uninstall k3s from worker(k3s-agent node)
    * ```shell
      systemctl stop k3s-agent
      /usr/local/bin/k3s-agent-uninstall.sh
      /usr/local/bin/k3s-uninstall.sh
      ```
3. uninstall k3s from control panel(k3s-server node)
    * ```shell
      systemctl stop k3s
      /usr/local/bin/k3s-uninstall.sh
      ```