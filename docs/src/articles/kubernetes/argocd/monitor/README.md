# kube-prometheus-stack

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `kube-prometheus-stack.yaml`
    * ```yaml
      <!-- @include: kube-prometheus-stack.yaml -->
      ```
2. prepare admin credentials secret
    * ```shell
      kubectl get namespaces monitor > /dev/null 2>&1 || kubectl create namespace monitor
      kubectl -n monitor create secret generic kube-prometheus-stack-credentials \
          --from-literal=grafana-username=admin \
          --from-literal=grafana-password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f kube-prometheus-stack.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/kube-prometheus-stack
      ```
5. NOTES
    * there may be a bug: sync again will continuous to submit patch job
    * there may be a bug: sync will failed but everything seems to work well

## visit grafana

* `grafana.dev.geekcity.tech` should be resolved to nginx-ingress
    + for example, add `$K8S_MASTER_IP grafana.dev.geekcity.tech` to `/etc/hosts`
* https://grafana.dev.geekcity.tech:32443
    + username
        * ```shell
          kubectl -n monitor get secret kube-prometheus-stack-credentials -o jsonpath='{.data.grafana-username}' | base64 -d
          ```
    + password
        * ```shell
          kubectl -n monitor get secret kube-prometheus-stack-credentials -o jsonpath='{.data.grafana-password}' | base64 -d
          ```

## visit prometheus

* `prometheus.dev.geekcity.tech` should be resolved to nginx-ingress
    + for example, add `$K8S_MASTER_IP prometheus.dev.geekcity.tech` to `/etc/hosts`
* https://prometheus.dev.geekcity.tech:32443

## monitor services

* [mariadb](../database/mariadb/README.md)

## references

* https://github.com/prometheus-operator/prometheus-operator
* https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack
* https://github.com/prometheus-operator/prometheus-operator/tree/main/Documentation/user-guides
* https://github.com/prometheus/mysqld_exporter/tree/main/mysqld-mixin
