# HA cluster of kubernetes

## Overview

- [HA cluster of kubernetes](#ha-cluster-of-kubernetes)
  - [Overview](#overview)
  - [install kubernetes with kubespray](#install-kubernetes-with-kubespray)
  - [install argocd](#install-argocd)
  - [prepare namespace for basic components](#prepare-namespace-for-basic-components)
  - [install ingress-nginx](#install-ingress-nginx)
  - [install storage components](#install-storage-components)
  - [install cert-manager](#install-cert-manager)
  - [install docker-registry](#install-docker-registry)
  - [install vcluster](#install-vcluster)

## install kubernetes with kubespray

1. reference: [kubespray](https://github.com/kubernetes-sigs/kubespray)
2. prepare nodes for kubernetes cluster
    * for example we have 3 nodes: node1(192.168.123.47), node2(192.168.123.151), node3(192.168.123.46)
    * ssh no password login for each node
    * configure sudo for each node
        + ```shell
          sudo bash -c "echo 'ben.wangz ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/extra"
          ```
    * turn off firewalld for each node
        + ```shell
          sudo systemctl stop firewalld && sudo systemctl disable firewalld
          ```
3. [prepare offline resource for kubespray](../offline-resource-prepare/README.md)
4. prepare environment for kubespray
    * ```shell
      podman run --rm -v $HOME/.ssh:/root/.ssh -it quay.io/kubespray/kubespray:v2.23.1 bash
      ```
5. generate configurations for kubespray
    * ```shell
      cp -rfp inventory/sample inventory/mycluster
      declare -a IPS=(192.168.123.47 192.168.123.151 192.168.123.46)
      CONFIG_FILE=inventory/mycluster/hosts.yaml python3 contrib/inventory_builder/inventory.py ${IPS[@]}
      # Review and change parameters under ``inventory/mycluster/group_vars``
      #less inventory/mycluster/group_vars/all/all.yml
      #less inventory/mycluster/group_vars/k8s_cluster/k8s-cluster.yml
      # modify `upstream_dns_servers in `inventory/mycluster/group_vars/all/all.yml`
      # uncomment `upstream_dns_servers` and add `223.5.5.5`, `223.6.6.6` to dns servers
      # https://github.com/kubernetes-sigs/kubespray/issues/9948
      vim inventory/mycluster/group_vars/all/all.yml
      ```
6. reset kubernetes cluster with ansible
    * ```shell
      # need to type 'yes'
      ansible-playbook -i inventory/mycluster/hosts.yaml --become --become-user=root reset.yml
      ```
7. install kubernetes cluster with ansible
    * ```shell
      # you may have to retry several times to install kubernetes cluster successfully for the bad network
      ansible-playbook -i inventory/mycluster/hosts.yaml --become --become-user=root cluster.yml
      ```
8. exit from container shell after installation
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
2. prepare [argocd.values.yaml](resources/argocd/argocd.values.yaml.md)
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
4. prepare [argocd-server-external.yaml](resources/argocd/argocd-server-external.yaml.md) and apply it
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

## prepare namespace for basic components

* ```shell
  kubectl create namespace basic-components
  ```

## install ingress-nginx

1. prepare [ingres-nginx.application.yaml](resources/argocd/applications/ingress-nginx/application.yaml.md)
2. apply the application to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f ingres-nginx.application.yaml
      ```
3. sync the application with argocd cli
    * ```shell
      argocd app sync ingress-nginx-application
      ```

## install storage components

1. take nfs provisioner as example
2. suppose we have a nfs server at node-storage
    * how to start nfs service with docker: [docker-nfsv4](../../../docker/software/nfs.md)
3. prepare [nfs-provisioner.application.yaml](resources/argocd/applications/nfs-provisioner/application.yaml.md)
4. apply the application to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f nfs-provisioner.application.yaml
      ```
5. sync the application with argocd cli
    * ```shell
      argocd app sync nfs-provisioner-application
      ```

## install cert-manager

1. prepare [cert-manager.application.yaml](resources/argocd/applications/cert-manager/application.yaml.md)
2. apply the application(cert-manager) to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f cert-manager.application.yaml
      ```
3. sync the application with argocd cli
    * ```shell
      argocd app sync cert-manager-application
      ```
4. prepare [alidns-webhook.application.yaml](resources/argocd/applications/cert-manager/alidns-webhook/application.yaml.md)
5. apply the application(alidns-webhook) to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f alidns-webhook.application.yaml
      ```
6. sync the application with argocd cli
    * ```shell
      argocd app sync alidns-webhook-application
      ```
7. prepare `access-key-id` and `access-key-secret` to manage your domain
    * ```shell
      # YOUR_ACCESS_KEY_ID=your-access-key-id
      # YOUR_ACCESS_KEY_SECRET=your-access-key-secret
      kubectl -n basic-components create secret generic alidns-webhook-secrets \
          --from-literal="access-token=$YOUR_ACCESS_KEY_ID" \
          --from-literal="secret-key=$YOUR_ACCESS_KEY_SECRET"
      ```
8. add permissions to your RAM account in aliyun
    * ```json
      {
        "Version": "1",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "alidns:AddDomainRecord",
              "alidns:DeleteDomainRecord"
            ],
            "Resource": "acs:alidns:*:*:domain/geekcity.tech"
          }, {
            "Effect": "Allow",
            "Action": [
              "alidns:DescribeDomains",
              "alidns:DescribeDomainRecords"
            ],
            "Resource": "acs:alidns:*:*:domain/*"
          }
        ]
      }
      ```
9. prepare cluster issuer definition [alidns.webhook.staging.issuer.yaml](resources/argocd/applications/cert-manager/alidns.webhook.cluster.issuer.yaml.md)
    * TODO this could be installed by argocd
10. add cluster issuer for cert-manager
    * ```shell
      kubectl -n basic-components apply -f alidns.webhook.staging.issuer.yaml
      ```

## install docker-registry

1. prepare [docker-registry.application.yaml](resources/argocd/applications/docker-registry/application.yaml.md)
2. prepare secrets
    * ```shell
      # create PASSWORD
      PASSWORD=($((echo -n $RANDOM | md5sum 2>/dev/null) | awk '{print $1}' || (echo -n $RANDOM | md5 2>/dev/null)))
      # Make htpasswd
      podman run --rm --entrypoint htpasswd docker.io/httpd:2.4.56-alpine3.17 -Bbn admin ${PASSWORD} > ${PWD}/htpasswd
      # NOTE: username should have at least 6 characters
      kubectl -n basic-components create secret generic docker-registry-secret \
          --from-literal=username=admin \
          --from-literal=password=${PASSWORD} \
          --from-file=${PWD}/htpasswd -o yaml --dry-run=client \
          | kubectl -n basic-components apply -f -
      rm -f ${PWD}/htpasswd
      echo $PASSWORD > docker.registry.password && chmod 600 docker.registry.password
      ```
3. apply the application to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f docker-registry.application.yaml
      ```
4. sync the application with argocd cli
    * ```shell
      argocd app sync docker-registry-application
      ```
5. check installation
    * ```shell
      IMAGE=busybox:1.33.1-uclibc \
          && DOCKER_REGISTRY_SERVICE=docker-registry.dev.geekcity.tech:32443 \
          && podman pull $IMAGE \
          && podman tag $IMAGE $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && PASSWORD=$(cat docker.registry.password) \
          && podman login -u admin -p $PASSWORD $DOCKER_REGISTRY_SERVICE \
          && podman push $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && podman image rm $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && podman pull $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && echo success
      # assert failed
      podman logout $DOCKER_REGISTRY_SERVICE \
          && podman pull $DOCKER_REGISTRY_SERVICE/$IMAGE
      ```

## install vcluster

1. prepare [vcluster.application.yaml](resources/argocd/applications/vcluster/application.yaml.md)
2. create namespace for vcluster
    * ```shell
      kubectl create namespace basic-vcluster
      ```
2. apply the application to k8s with kubectl
    * ```shell
      kubectl -n argocd apply -f vcluster.application.yaml
      ```
3. sync the application with argocd cli
    * ```shell
      argocd app sync vcluster-application
      ```
4. download vcluster client
    * ```shell
      curl -Lo $HOME/bin/vcluster "https://github.com/loft-sh/vcluster/releases/latest/download/vcluster-linux-amd64" \
          && chmod u+x $HOME/bin/vcluster
      ```
5. how to connect to specific vcluster
    * ```shell
      vcluster connect basic-vcluster -- bash
      # communicate with vcluster using kubectl/helm
      # kubectl get pods -A
      # just using `exit` to disconnect from vcluster
      ```
