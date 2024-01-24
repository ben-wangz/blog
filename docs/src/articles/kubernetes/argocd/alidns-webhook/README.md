# alidns-webhook

## references

* https://github.com/DEVmachine-fr/cert-manager-alidns-webhook

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `alidns-webhook.yaml`
    * ```yaml
      <!-- @include: alidns-webhook.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f alidns-webhook.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/alidns-webhook
      ```
4. create secret of `alidns-webhook-secrets`
    * ```shell
      #YOUR_ACCESS_KEY_ID=xxxxxxxxxxx
      #YOUR_ACCESS_KEY_SECRET=yyyyyyyyyyy
      kubectl -n basic-components create secret generic alidns-webhook-secrets \
          --from-literal="access-token=$YOUR_ACCESS_KEY_ID" \
          --from-literal="secret-key=$YOUR_ACCESS_KEY_SECRET"
      ```
5. prepare `alidns-webhook-cluster-issuer.yaml`
    * ```yaml
      <!-- @include: alidns-webhook-cluster-issuer.yaml -->
      ```
6. apply cluster issuer to k8s
    * ```shell
      kubectl apply -f alidns-webhook-cluster-issuer.yaml
      ```