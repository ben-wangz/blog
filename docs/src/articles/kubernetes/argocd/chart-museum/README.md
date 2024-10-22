# chart-museum

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready
5. [minio](../storage/minio/minio.md) is ready

## installation

1. prepare credentials secret
    * ```shell
      kubectl get namespaces basic-components > /dev/null 2>&1 || kubectl create namespace basic-components
      kubectl -n basic-components create secret generic chart-museum-credentials \
          --from-literal=username=admin \
          --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16) \
          --from-literal=aws_access_key_id=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d) \
          --from-literal=aws_secret_access_key=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      ```
2. prepare `chart-museum.yaml`
    * ::: code-tabs#shell
      @tab pvc backend
      ```yaml
      <!-- @include: chart-museum-pvc.yaml -->
      ```
      @tab minio backend
      ```yaml
      <!-- @include: chart-museum-minio.yaml -->
      ```
      :::
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f chart-museum.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/chart-museum
      ```
5. prepare minio bucket if you choose to use minio as backend
    * ::: code-tabs#shell
      @tab pvc backend
      ```yaml
      ```
      @tab minio backend
      ```yaml
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc mb --ignore-existing minio/chart-museum"
      ```
      :::
6. patch to resolve minio endpoint if you choose to use minio as backend
    * ::: code-tabs#shell
      @tab pvc backend
      ```shell
      ```
      @tab minio backend
      ```shell
      kubectl -n basic-components patch deployment chart-museum-chartmuseum \
          --type merge \
          --patch '{"spec":{"template":{"spec":{"hostAliases":[{"ip":"192.168.49.2","hostnames":["minio-api.dev.geekcity.tech"]}]}}}}'
      ```
      :::

## tests

1. create a chart
    * ```shell
      podman run --rm \
          -v $(pwd):/code \
          --workdir /code \
          -it m.zjvis.net/docker.io/alpine/k8s:1.29.4 \
          helm create demo-chart
      ```
1. publish a chart
    * ```shell
      podman run --rm \
          --add-host chart-museum.dev.geekcity.tech:$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}') \
          -v $(pwd):/code \
          --env HELM_REPO_USERNAME=admin \
          --env HELM_REPO_PASSWORD=$(kubectl -n basic-components get secret chart-museum-credentials -o jsonpath='{.data.password}' | base64 -d) \
          -it m.zjvis.net/docker.io/alpine/k8s:1.29.4 \
          helm cm-push --insecure \
              /code/demo-chart https://chart-museum.dev.geekcity.tech:32443 \
              --context-path /
      ```
