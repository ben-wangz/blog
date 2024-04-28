# gitea

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `gitea.yaml`
    * ```yaml
      <!-- @include: gitea.yaml -->
      ```
2. prepare admin credentials secret
    * ```shell
      kubectl get namespaces application > /dev/null 2>&1 || kubectl create namespace application
      kubectl -n application create secret generic gitea-admin-credentials \
          --from-literal=username=gitea_admin \
          --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f gitea.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/gitea
      ```

## tests

1. with browser
    * gitea.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP gitea.dev.geekcity.tech` to `/etc/hosts`
    * https://gitea.dev.geekcity.tech:32443
        + username
            * ```shell
              kubectl -n application get secret gitea-admin-credentials -o jsonpath='{.data.username}' | base64 -d
              ```
        + password
            * ```shell
              kubectl -n application get secret gitea-admin-credentials -o jsonpath='{.data.password}' | base64 -d
              ```
2. with ssh(git client)
    * ssh.gitea.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP ssh.gitea.dev.geekcity.tech` to `/etc/hosts`
