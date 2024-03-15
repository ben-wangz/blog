# argo-workflows

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `argo-workflows.yaml`
    * ```yaml
      <!-- @include: argo-workflows.yaml -->
      ```
2. prepare argo workflow client binary
    * ```shell
      <!-- @include: @src/articles/kubernetes/binary/download_argo_workflow_binary.sh -->
      ```
3. create `business-workflows` namespace to be controlled
    * NOTE: this should be set with `controller.workflowNamespaces` in the values of `argo-workflows.yaml`
    * ```shell
      kubectl get namespace business-workflows > /dev/null 2>&1 || kubectl create namespace business-workflows
      ```
4. apply to k8s
    * ```shell
      kubectl -n argocd apply -f argo-workflows.yaml
      ```
5. sync by argocd
    * ```shell
      argocd app sync argocd/argo-workflows
      ```

## tests

### hello world

1. prepare `hello-world.yaml`
    * ```yaml
      <!-- @include: hello-world.yaml -->
      ```
2. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit hello-world.yaml
      ```
3. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get hello-world-m5kqf
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs hello-world-m5kqf
      argo -n business-workflows logs @latest
      ```

### others

1. [publish container image](publish-container-image.md)
2. [deploy argocd app](deploy-argocd-app.md)
