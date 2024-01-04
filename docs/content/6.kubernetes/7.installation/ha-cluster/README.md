# HA cluster of kubernetes

## Overview

- [HA cluster of kubernetes](#ha-cluster-of-kubernetes)
  - [Overview](#overview)
  - [install kubernetes with kubespray](#install-kubernetes-with-kubespray)
  - [install argocd](#install-argocd)

## install kubernetes with kubespray

1. reference: [kubespray](https://github.com/kubernetes-sigs/kubespray)
2. operate nodes with root
3. prepare nodes for kubernetes cluster
    * for example we have 3 nodes: node1(192.168.1.47), node2(192.168.1.151), node3(192.168.1.46)
    * ssh no password login for each node
    * turn off selinux and firewalld for each node
        + ```shell
          sed -i 's/^SELINUX=.*/SELINUX=disabled/' /etc/selinux/config && setenforce 0
          ```
        + ```shell
          systemctl stop firewalld && systemctl disable firewalld
          ```
4. prepare for container runtime environment
    * ```shell
      dnf -y install podman
      CONTAINER_RUNTIME=podman
      KUBESPRAY_IMAGE=quay.io/kubespray/kubespray:v2.23.1
      ```
5. copy templates for kubespray installation
    * ```shell
      KUBESPRAY_INSTALLATION_DIRECTORY=$HOME/kubespray-installation
      mkdir -p $KUBESPRAY_INSTALLATION_DIRECTORY
      $CONTAINER_RUNTIME run --rm \
          -v $KUBESPRAY_INSTALLATION_DIRECTORY:/kubespray-installation \
          -it $KUBESPRAY_IMAGE \
          /bin/cp -rfp /kubespray/inventory/sample /kubespray-installation/inventory
      ```
6. generate configurations for kubespray
    * ```shell
      INVENTORY_DIRECTORY=$KUBESPRAY_INSTALLATION_DIRECTORY/inventory
      declare -a IPS=(172.25.181.64)
      $CONTAINER_RUNTIME run --rm \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -e CONFIG_FILE=/kubespray-installation/inventory/hosts.yaml \
          -it $KUBESPRAY_IMAGE \
          python3 /kubespray/contrib/inventory_builder/inventory.py ${IPS[@]}
      ```
    * ```shell
      cat << 'EOF' >> $INVENTORY_DIRECTORY/group_vars/all/all.yml
      upstream_dns_servers:
        - 223.5.5.5
        - 223.6.6.6
      EOF
      ```
    * ```shell
      sed -i -E 's/^auto_renew_certificates: false/auto_renew_certificates: true/g' $INVENTORY_DIRECTORY/group_vars/k8s_cluster/k8s-cluster.yml
      sed -i -E 's@^# containerd_storage_dir: "/var/lib/containerd"@containerd_storage_dir: "/data/containerd"@g' $INVENTORY_DIRECTORY/group_vars/all/containerd.yml
      ```
    * using DaoCloud mirrors
        + ```shell
          # replace files_repo, kube_image_repo, gcr_image_repo, github_image_repo, docker_image_repo and quay_image_repo
          sed -i 's@^# files_repo: .*@files_repo: "https://files.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          sed -i 's@^# kube_image_repo: .*@kube_image_repo: "k8s.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          sed -i 's@^# gcr_image_repo: .*@gcr_image_repo: "gcr.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          sed -i 's@^# github_image_repo: .*@github_image_repo: "ghcr.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          sed -i 's@^# docker_image_repo: .*@docker_image_repo: "docker.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          sed -i 's@^# quay_image_repo: .*@quay_image_repo: "quay.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          # uncomment lines with files_repo and registry_host
          sed -i -E '/# .*\{\{ files_repo/s/^# //g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
          ```
        + if you want to configure you own services, try [offline-resource-prepare](../offline-resource-prepare/README.md)
7. reset kubernetes cluster with ansible
    * ```shell
      # need to type 'yes'
      $CONTAINER_RUNTIME run --rm \
          -v $HOME/.ssh:/root/.ssh:ro \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -it $KUBESPRAY_IMAGE \
          ansible-playbook -i /kubespray-installation/inventory/hosts.yaml --become --become-user=root reset.yml
      ```
8. install kubernetes cluster with ansible
    * ```shell
      # you may have to retry several times to install kubernetes cluster successfully for the bad network
      $CONTAINER_RUNTIME run --rm \
          -v $HOME/.ssh:/root/.ssh:ro \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -it $KUBESPRAY_IMAGE \
          ansible-playbook -i /kubespray-installation/inventory/hosts.yaml --become --become-user=root cluster.yml
      ```
9. copy configurations for kubectl
    * ```shell
      mkdir ~/.kube \
          && sudo cp /etc/kubernetes/admin.conf ~/.kube/config \
          && sudo chown ben.wangz:ben.wangz ~/.kube/config \
          && chmod 600 ~/.kube/config
      ```
10. download helm client
    * ```shell
      # asuming that $HOME/bin is in $PATH
      mkdir -p $HOME/bin \
          && curl -sSL -o $HOME/bin/helm.tar.gz https://get.helm.sh/helm-v3.7.0-linux-amd64.tar.gz \
          && tar -zxf $HOME/bin/helm.tar.gz -C $HOME/bin/ \
          && mv $HOME/bin/linux-amd64/helm $HOME/bin/ \
          && chmod u+x $HOME/bin/helm \
          && rm -rf $HOME/bin/linux-amd64 $HOME/bin/helm.tar.gz
      ```

## install argocd

1. argocd will be used to install all the other components in gitOps way
2. prepare argocd.values.yaml
    * ::code-group
        ```yaml [argocd.values.yaml]
        crds:
          install: true
          keep: false
        ```
      ::
3. install argocd with helm
    * ```shell
      helm install my-argo-cd argo-cd \
          --namespace argocd \
          --create-namespace \
          --version 5.46.7 \
          --repo https://argoproj.github.io/argo-helm \
          --values argocd.values.yaml \
          --atomic
      ```
4. prepare argocd-server-external.yaml
    * ::code-group
        ```yaml [argocd-server-external.yaml]
        apiVersion: v1
        kind: Service
        metadata:
          labels:
            app.kubernetes.io/component: server
            app.kubernetes.io/instance: my-argo-cd
            app.kubernetes.io/name: argocd-server-external
            app.kubernetes.io/part-of: argocd
            app.kubernetes.io/version: v2.8.4
          name: argocd-server-external
        spec:
          ports:
          - name: https
            port: 443
            protocol: TCP
            targetPort: 8080
            nodePort: 30443
          selector:
            app.kubernetes.io/instance: my-argo-cd
            app.kubernetes.io/name: argocd-server
          type: NodePort
        ```
      ::
    * ```shell
      kubectl -n argocd apply -f argocd-server-external.yaml
      ```
5. download argocd cli
    * ```shell
      # asuming that $HOME/bin is in $PATH
      mkdir -p $HOME/bin \
          && curl -sSL -o $HOME/bin/argocd https://github.com/argoproj/argo-cd/releases/download/v2.8.4/argocd-linux-amd64 \
          && chmod u+x $HOME/bin/argocd
      ```
6. get argocd intial password
    * ```shell
      kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
      ```
7. login argocd
    * ```shell
      # you need to typein the password
      argocd login --insecure --username admin node1:30443
      ```
8. change admin password
    * ```shell
      argocd account update-password
      ```
