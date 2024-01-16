# deployKF(installing Kubeflow)

## overview
- [deployKF(installing Kubeflow)](#deploykfinstalling-kubeflow)
  - [overview](#overview)
  - [prerequisites](#prerequisites)
  - [installation](#installation)

## prerequisites

1. vcluster installed
2. connect to vcluster(`basic-vcluster` as example in this doc)
    * ```shell
      vcluster connect basic-vcluster -- bash
      ```

## installation

1. modprobe(each node)
    * ```shell
      MODULES_FILE=/etc/modules-load.d/istio.conf
      sudo bash -c "echo '# These modules need to be loaded on boot so that Istio (as required by Kubeflow) runs properly' > $MODULES_FILE"
      sudo bash -c "echo '# See also: https://github.com/istio/istio/issues/23009' >> $MODULES_FILE"
      modules=(br_netfilter nf_nat xt_REDIRECT xt_owner iptable_nat iptable_mangle iptable_filter)
      for module in ${modules[@]}
      do
          sudo modprobe $module
          sudo bash -c "echo '$module' >> /etc/modules-load.d/istio.conf"
      done
      ```
2. install argocd
    * prepare [argocd.values.yaml](resources/deploykf/argocd.values.yaml.md)
    * install argocd with helm
        + ```shell
          helm install my-argo-cd argo-cd \
              --namespace argocd \
              --create-namespace \
              --version 5.46.7 \
              --repo https://argoproj.github.io/argo-helm \
              --values argocd.values.yaml \
              --atomic
          ```
    * prepare [argocd-server-external.yaml](resources/deploykf/argocd-server-external.yaml.md) and apply it
        + ```shell
          kubectl -n argocd apply -f argocd-server-external.yaml
          ```
    * download argocd cli
        + ```shell
          # asuming that $HOME/bin is in $PATH
          mkdir -p $HOME/bin \
              && curl -sSL -o $HOME/bin/argocd https://github.com/argoproj/argo-cd/releases/download/v2.8.4/argocd-linux-amd64 \
              && chmod u+x $HOME/bin/argocd
          ```
    * get argocd intial password
        + ```shell
          kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
          ```
    * login argocd
        + ```shell
          argocd login --insecure --username admin node1:31443
          ```
3. git clone
    * ```shell
      git clone https://github.com/ben-wangz/deploykf-example.git && cd deploykf-example
      ```
4. apply deploykf application to cluster
    * ```shell
      kubectl -n argocd apply -f generate/manifests/app-of-apps.yaml
      ```
5. sync step by step
    * ```shell
      argocd app sync -l "app.kubernetes.io/name=deploykf-app-of-apps"
      #argocd app sync -l "app.kubernetes.io/name=deploykf-namespaces"
      argocd app sync -l "app.kubernetes.io/component=deploykf-dependencies"
      argocd app sync -l "app.kubernetes.io/component=deploykf-core"
      argocd app sync -l "app.kubernetes.io/component=deploykf-opt"
      #argocd app sync -l "app.kubernetes.io/component=deploykf-tools"
      argocd app sync -l "app.kubernetes.io/component=kubeflow-dependencies"
      argocd app sync -l "app.kubernetes.io/component=kubeflow-tools"
      ```
6. port-forwarding
    * ```shell
      kubectl port-forward --namespace "deploykf-istio-gateway" svc/deploykf-gateway 8080:http 8443:https --address 0.0.0.0
      ```
7. configure you hosts, add following lines to `/etc/hosts`, you may change `127.0.0.1` to the ip of your cluster node
    * ```text
      127.0.0.1 deploykf.example.com
      127.0.0.1 argo-server.deploykf.example.com
      127.0.0.1 minio-api.deploykf.example.com
      127.0.0.1 minio-console.deploykf.example.com
      ```
8. open browser and visit [deploykf.example.com](https://deploykf.example.com)
    * username: user1@example.com
    * password: user1

