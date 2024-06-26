# argocd

## prepare

1. k8s is ready
2. helm binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_helm_binary.sh -->
      ```
3. argocd binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_argocd_binary.sh -->
      ```

## install

1. prepare `argocd.values.yaml`
    * ```yaml
      <!-- @include: argocd.values.yaml -->
      ```
2. install argocd with helm
    * ```shell
      helm install argo-cd argo-cd \
          --namespace argocd \
          --create-namespace \
          --version 5.46.7 \
          --repo https://ben-wangz.github.io/helm-chart-mirror/charts \
          --values argocd.values.yaml \
          --atomic
      ```
3. prepare `argocd-server-external.yaml`
    * ```yaml
      <!-- @include: argocd-server-external.yaml -->
      ```
4. apply `argocd-server-external.yaml` to k8s
    * ```shell
      kubectl -n argocd apply -f argocd-server-external.yaml
      ```
5. get argocd initial password
    * ```shell
      kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
      ```
6. login with argocd cli
    * ```shell
      FIRST_MASTER_IP=$(kubectl get nodes --selector=node-role.kubernetes.io/control-plane -o jsonpath='{$.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      argocd login --insecure --username admin $FIRST_MASTER_IP:30443
      ```
7. login with browser
    * open https://k8s-master:30443
    * username: admin
    * password: the password you get in step 5
8. change admin password
    * optional for dev environment
    * ```shell
      argocd account update-password
      ```
