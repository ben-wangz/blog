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

1. prepare `values.yaml`
    * ```yaml
      <!-- @include: values.yaml -->
      ```
2. install argocd with helm
    * ```shell
      helm install argo-cd argo-cd \
          --namespace argocd \
          --create-namespace \
          --version 8.1.2 \
          --repo https://argoproj.github.io/argo-helm \
          --values values.yaml \
          --atomic
      ```
3. get argocd initial password
    * ```shell
      kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d && echo
      ```
4. login with argocd cli
    * prepare `login.sh`
        + ```bash
          <!-- @include: @src/articles/kubernetes/helm/argocd/login.sh -->
          ```
    * ```shell
      bash login.sh
      ```
5. login with browser
    * open https://k8s-master:30443
    * username: admin
    * password: the password you get in step 5
8. change admin password
    * optional for dev environment
    * ```shell
      argocd account update-password
      ```
