# install k8s by kubespray

## references

1. [kubespray](https://github.com/kubernetes-sigs/kubespray)

## prepare

1. nodes for kubernetes cluster
    * for example we have 3 nodes: node1(192.168.1.47), node2(192.168.1.151), node3(192.168.1.46)
    * it's okay if you want a k8s with only single master node
    * define the IPS variable
        + ```shell
          declare -a IPS=(192.168.1.47 192.168.1.151 192.168.1.46)
          ```
2. os for each node: [fedora 38](#) <!-- TODO installation doc in linux category -->
3. root account required
4. configure ssh no password login for each node
5. turn off selinux and firewalld of each node
    * ```shell
      <!-- @include: @src/articles/commands/linux/turn-off-selinux.sh -->
      <!-- @include: @src/articles/commands/linux/turn-off-firewalld.sh -->
      ```
6. prepare for container runtime environment
    * ```shell
      dnf -y install podman
      CONTAINER_RUNTIME=podman
      KUBESPRAY_IMAGE=quay.io/kubespray/kubespray:v2.23.1
      ```

## custom k8s cluster by configurations

1. copy templates for kubespray installation
    * ```shell
      KUBESPRAY_INSTALLATION_DIRECTORY=$HOME/kubespray-installation
      mkdir -p $KUBESPRAY_INSTALLATION_DIRECTORY
      $CONTAINER_RUNTIME run --rm \
          -v $KUBESPRAY_INSTALLATION_DIRECTORY:/kubespray-installation \
          -it $KUBESPRAY_IMAGE \
          /bin/cp -rfp /kubespray/inventory/sample /kubespray-installation/inventory
      ```
2. generate configurations for kubespray
    * ```shell
      # NOTE: IPS variable defined in prepare section
      INVENTORY_DIRECTORY=$KUBESPRAY_INSTALLATION_DIRECTORY/inventory
      $CONTAINER_RUNTIME run --rm \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -e CONFIG_FILE=/kubespray-installation/inventory/hosts.yaml \
          -it $KUBESPRAY_IMAGE \
          python3 /kubespray/contrib/inventory_builder/inventory.py ${IPS[@]}
      ```
3. configure dns servers: [NodeLocalDNS Loop detected for zone "."](https://github.com/kubernetes-sigs/kubespray/issues/9948#issuecomment-1533876540)
    * ```shell
      cat << 'EOF' >> $INVENTORY_DIRECTORY/group_vars/all/all.yml
      upstream_dns_servers:
        - 223.5.5.5
        - 223.6.6.6
      EOF
      ```
4. turn on auto renew certificates
    * ```shell
      sed -i -E 's/^auto_renew_certificates: false/auto_renew_certificates: true/g' $INVENTORY_DIRECTORY/group_vars/k8s_cluster/k8s-cluster.yml
      ```
5. configure containerd storage directory to `/data/containerd`
    * ```shell
      sed -i -E 's@^# containerd_storage_dir: "/var/lib/containerd"@containerd_storage_dir: "/data/containerd"@g' $INVENTORY_DIRECTORY/group_vars/all/containerd.yml
      ```
5. using DaoCloud mirrors
    + ```shell
      # replace files_repo, kube_image_repo, gcr_image_repo, github_image_repo, docker_image_repo and quay_image_repo
      sed -i 's@^# files_repo: .*@files_repo: "https://files.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      sed -i 's@^# kube_image_repo: .*@kube_image_repo: "k8s.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      sed -i 's@^# gcr_image_repo: .*@gcr_image_repo: "gcr.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      sed -i 's@^# github_image_repo: .*@github_image_repo: "ghcr.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      sed -i 's@^# docker_image_repo: .*@docker_image_repo: "docker.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      sed -i 's@^# quay_image_repo: .*@quay_image_repo: "quay.m.daocloud.io"@g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      # uncomment lines with files_repo
      sed -i -E '/# .*\{\{ files_repo/s/^# //g' $INVENTORY_DIRECTORY/group_vars/all/offline.yml
      ```
    + TODO you may configure you own services, try [kubespray-offline-resource-prepare](kubespray-offline-resource-prepare.md)

## install k8s by kubespray

1. reset kubernetes cluster with ansible
    * optional if your cluster is clean
    * ```shell
      # need to type 'yes'
      $CONTAINER_RUNTIME run --rm \
          -v $HOME/.ssh:/root/.ssh:ro \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -it $KUBESPRAY_IMAGE \
          ansible-playbook -i /kubespray-installation/inventory/hosts.yaml --become --become-user=root reset.yml
      ```
2. install kubernetes cluster with ansible
    * ```shell
      # you may have to retry several times to install kubernetes cluster successfully for the bad network
      $CONTAINER_RUNTIME run --rm \
          -v $HOME/.ssh:/root/.ssh:ro \
          -v $INVENTORY_DIRECTORY:/kubespray-installation/inventory \
          -it $KUBESPRAY_IMAGE \
          ansible-playbook -i /kubespray-installation/inventory/hosts.yaml --become --become-user=root cluster.yml
      ```
3. copy kubeconfig to local
    * ```shell
      mkdir $HOME/.kube \
          && cp /etc/kubernetes/admin.conf $HOME/.kube/config \
          && chown $UID:$UID $HOME/.kube/config \
          && chmod 600 $HOME/.kube/config
      ```

## addtional software
1. [install argocd by helm](../helm/argocd/README.md)
2. [install ingress by argocd](../argocd/ingress/README.md)
3. [install cert-manager by argocd](../argocd/cert-manager/README.md)
