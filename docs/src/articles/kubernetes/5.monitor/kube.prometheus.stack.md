# kube-prometheus-stack

## main usage

* collect metrics which show the status of nodes in k8s cluster
* collect metrics which show the status of k8s cluster
* collect metrics generated by pods in k8s
* send metrics to prometheus and visualized by grafana in real time

## conceptions

* [official docs](https://github.com/prometheus-operator/prometheus-operator)
* [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack)
  is a helm chart for [kube-prometheus](https://github.com/prometheus-operator/kube-prometheus)
* kube-prometheus is a package contains
    + The [Prometheus Operator](https://github.com/prometheus-operator/prometheus-operator)
    + Highly available [Prometheus](https://prometheus.io/)
    + Highly available [Alertmanager](https://github.com/prometheus/alertmanager)
    + [Prometheus node-exporter](https://github.com/prometheus/node_exporter)
    + [Prometheus Adapter for Kubernetes Metrics APIs](https://github.com/DirectXMan12/k8s-prometheus-adapter)
    + [kube-state-metrics](https://github.com/kubernetes/kube-state-metrics)
    + [Grafana](https://grafana.com/)
* what does `prometheus-operator` configuration actions when applying a `ServiceMonitor`
    + image below is downloaded
      from `https://github.com/prometheus-operator/prometheus-operator/blob/main/Documentation/troubleshooting.md`
    + ![configuration-actions-when-applying-a-service-monitor.png](
      resources/kube.prometheus.stack/configuration-actions-when-applying-a-service-monitor.png)
* more detailed usage documents for `prometheus-operator`:
  [user-guides of prometheus-operator](
  https://github.com/prometheus-operator/prometheus-operator/tree/main/Documentation/user-guides)
* more detailed information for the dashboard(mysql_exporter) imported to grafana:
  [mysqld-mixin from mysqld-exporter](https://github.com/prometheus/mysqld_exporter/tree/main/mysqld-mixin)

## purpose

* prepare a kind cluster with basic components(one master and three workers)
* setup kube prometheus stack
* create prometheus custom resources
* check metrics of nodes in k8s cluster in real time
* check metrics of k8s cluster in real time
* test with metrics generated by mysql service in real time

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
    * modify `kind.cluster.yaml` to [kind.cluster.with.3.workers.yaml](../resources/kind.cluster.with.3.workers.yaml.md)
    * add hosts info to every worker node(master node has already been added)
        + ```shell
          for NODE in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec $NODE bash -c 'echo 172.17.0.1 docker.registry.local >> /etc/hosts' \
                  && docker exec $NODE bash -c 'echo 172.17.0.1 insecure.docker.registry.local >> /etc/hosts' \
                  && docker exec $NODE bash -c 'echo 172.17.0.1 chart.museum.local >> /etc/hosts'
          done
          ```
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="kube.prometheus.stack.monitor"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_quay.io_prometheus_alertmanager_v0.23.0.dim" \
          "docker.io_grafana_grafana_8.3.5.dim" \
          "docker.io_bats_bats_v1.4.1.dim" \
          "docker.io_curlimages_curl_7.73.0.dim" \
          "docker.io_busybox_1.31.1.dim" \
          "docker.io_quay.io_kiwigrid_k8s-sidecar_1.15.1.dim" \
          "docker.io_grafana_grafana-image-renderer_3.4.0.dim" \
          "docker.io_k8s.gcr.io_kube-state-metrics_kube-state-metrics_v2.3.0.dim" \
          "docker.io_quay.io_prometheus_node-exporter_v1.3.1.dim" \
          "docker.io_k8s.gcr.io_ingress-nginx_kube-webhook-certgen_v1.0.dim" \
          "docker.io_quay.io_prometheus-operator_prometheus-operator_v0.54.0.dim" \
          "docker.io_quay.io_prometheus-operator_prometheus-config-reloader_v0.54.0.dim" \
          "docker.io_quay.io_thanos_thanos_v0.24.0.dim" \
          "docker.io_quay.io_prometheus_prometheus_v2.33.1.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_mysqld-exporter_0.13.0-debian-10-r56.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace monitor > /dev/null 2>&1 || kubectl create namespace monitor \
              && kubectl -n monitor apply -f self.signed.and.ca.issuer.yaml
          ```
4. install kube prometheus stack
    * prepare [kube.prometheus.stack.values.yaml](resources/kube.prometheus.stack/kube.prometheus.stack.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/quay.io/prometheus/alertmanager:v0.23.0" \
              "docker.io/grafana/grafana:8.3.5" \
              "docker.io/bats/bats:v1.4.1" \
              "docker.io/curlimages/curl:7.73.0" \
              "docker.io/busybox:1.31.1" \
              "docker.io/quay.io/kiwigrid/k8s-sidecar:1.15.1" \
              "docker.io/grafana/grafana-image-renderer:3.4.0" \
              "docker.io/k8s.gcr.io/kube-state-metrics/kube-state-metrics:v2.3.0" \
              "docker.io/quay.io/prometheus/node-exporter:v1.3.1" \
              "docker.io/k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0" \
              "docker.io/quay.io/prometheus-operator/prometheus-operator:v0.54.0" \
              "docker.io/quay.io/prometheus-operator/prometheus-config-reloader:v0.54.0" \
              "docker.io/quay.io/thanos/thanos:v0.24.0" \
              "docker.io/quay.io/prometheus/prometheus:v2.33.1"
          ```
    * install by helm
        + ```shell
          # NOTE: kind start etcd on port 2381 instead of 2379
          # NOTE: for kind, x509 certificate is valid for 127.0.0.1, but not valid for the $REAL_IP
          helm install \
              --create-namespace --namespace monitor \
              my-kube-prometheus-stack \
              https://resource.geekcity.tech/kubernetes/charts/https/prometheus-community.github.io/helm-charts/kube-prometheus-stack-32.2.1.tgz \
              --values kube.prometheus.stack.values.yaml \
              --set kubeEtcd.service.targetPort=2381 \
              --set kubeScheduler.serviceMonitor.insecureSkipVerify=true \
              --atomic
          ```

## test

1. visit `https://grafana.local`
    * extract username and password
        + ```shell
          kubectl -n monitor get secret my-kube-prometheus-stack-grafana -o jsonpath="{.data.admin-user}" \
              | base64 --decode \
              && echo
          kubectl -n monitor get secret my-kube-prometheus-stack-grafana -o jsonpath="{.data.admin-password}" \
              | base64 --decode \
              && echo
          ```
    * check `Node Exporter/*` to figure out the status of nodes in k8s cluster
    * check `Kubernetes/*` to figure out the status of k8s cluster
2. install `mariadb` with metrics feature enabled
    * prepare [mariadb.values.yaml](resources/kube.prometheus.stack/mariadb.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/mariadb:10.5.12-debian-10-r0" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r153" \
              "docker.io/bitnami/mysqld-exporter:0.13.0-debian-10-r56"
          ```
    * install `mariadb`
        + ```shell
          helm install \
              --create-namespace --namespace test \
              mariadb-for-test \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/mariadb-9.4.2.tgz \
              --values mariadb.values.yaml \
              --atomic
          ```
    * add `Prometheus` instance
        + add service account for prometheus
            * prepare [service.account.for.test.yaml](resources/kube.prometheus.stack/service.account.for.test.yaml.md)
            * apply to k8s cluster
                + ```shell
                  kubectl -n test apply -f service.account.for.test.yaml
                  ```
        + prepare [prometheus.for.test.yaml](resources/kube.prometheus.stack/prometheus.for.test.yaml.md)
        + apply to k8s cluster
            * ```shell
              kubectl -n test apply -f prometheus.for.test.yaml
              ```
        + wait for prometheus to be ready
            * ```shell
              kubectl -n test wait --for=condition=ready pod --all
              ```
        + add `$IP test-prometheus.local` to hosts
        + visit `http://test-prometheus.local` to check prometheus service ready
    * add `ServiceMonitor` instance
        + prepare [service.monitor.for.test.yaml](resources/kube.prometheus.stack/service.monitor.for.test.yaml.md)
        + apply to k8s cluster
            * ```shell
              kubectl -n test apply -f service.monitor.for.test.yaml
              ```
    * configure `test-prometheus` as a new datasource to grafana
        + name = `test-prometheus`
        + http.url = `http://prometheus-operated.test:9090`
        + keep other configuration the same as defaults
        + click `Save & Test`
    * import dashboard to grafana
        + prepare
          [mysql.mixin.dashboard.grafana.json](resources/kube.prometheus.stack/mysql.mixin.dashboard.grafana.json.md)
        + import `mysql.mixin.dashboard.grafana.json` to dashboard
        + now you can check the dashboard named `General/MySQL Exporter Quickstart and Dashboard`
            * select `test-prometheus` as `datasource`
    * delete service account, `test-prometheus` and `ServiceMonitor`
        + ```shell
          kubectl -n test delete -f service.monitor.for.test.yaml
          kubectl -n test delete -f prometheus.for.test.yaml
          kubectl -n test delete -f service.account.for.test.yaml
          kubectl -n test delete pvc prometheus-test-prometheus-db-prometheus-test-prometheus-0
          ```
    * uninstall `mariadb-for-test`
        + ```shell
          helm -n test uninstall mariadb-for-test
          kubectl -n test delete pvc data-mariadb-for-test-0
          ```

## uninstallation

1. uninstall `kube-prometheus-stack`
    * ```shell
      helm -n monitor uninstall my-kube-prometheus-stack
      kubectl -n monitor delete pvc alertmanager-my-kube-prometheus-stack-alertmanager-db-alertmanager-my-kube-prometheus-stack-alertmanager-0
      kubectl -n monitor delete pvc prometheus-my-kube-prometheus-stack-prometheus-db-prometheus-my-kube-prometheus-stack-prometheus-0
      # NOTE: my-kube-prometheus-stack-grafana will be deleted automatically after uninstallation of my-kube-prometheus-stack
      #kubectl -n monitor delete pvc my-kube-prometheus-stack-grafana
      ```
