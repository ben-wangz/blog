# cert-manager

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `cert-manager.yaml`
    * ```yaml
      <!-- @include: cert-manager.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f cert-manager.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/cert-manager
      ```

## bootstrapping self-signed CA cluster issuer

1. prepare `self-signed.yaml`
    <!-- may be a bug: cannot import the whole file of self-signed.yaml -->
    * ```yaml
      <!-- @include: self-signed.yaml{1-8} -->
      <!-- @include: self-signed.yaml{9-} -->
      ```
2. apply to k8s
    * ```shell
      kubectl apply -f self-signed.yaml
      ```
