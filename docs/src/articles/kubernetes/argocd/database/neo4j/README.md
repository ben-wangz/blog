# neo4j

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `neo4j.yaml`
    * ```yaml
      <!-- @include: neo4j.yaml -->
      ```
2. prepare credentials secret
    * admin username must be `neo4j`
    * ```shell
      kubectl get namespaces database > /dev/null 2>&1 || kubectl create namespace database
      kubectl -n database create secret generic neo4j-credentials \
          --from-literal=NEO4J_AUTH=neo4j/$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f neo4j.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/neo4j
      ```
5. expose service with ingress
    * prepare `neo4j-reverse-proxy.yaml`
        + ```yaml
            <!-- @include: neo4j-reverse-proxy.yaml -->
          ```
    * apply to k8s
        + ```shell
          kubectl -n argocd apply -f neo4j-reverse-proxy.yaml
          ```
    * sync by argocd
        + ```shell
          argocd app sync argocd/neo4j-reverse-proxy
          ```

## tests

1. extract neo4j credentials
    * ```shell
      kubectl -n database get secret neo4j-credentials -o jsonpath='{.data.NEO4J_AUTH}' | base64 -d
      ```
2. with http
    * neo4j.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP neo4j.dev.geekcity.tech` to `/etc/hosts`
    * open browser and visit `https://neo4j.dev.geekcity.tech:32443`
