# TiDB

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. local storage class is ready

## installation

1. prepare resources
    * `tidb-operator-crd.yaml`
        + ```yaml
          <!-- @include: tidb-operator-crd.yaml -->
          ```
    * `tidb-operator.yaml`
        + ```yaml
          <!-- @include: tidb-operator.yaml -->
          ```
    * `tidb-cluster.yaml`
        + ```yaml
          <!-- @include: tidb-cluster.yaml -->
          ```
    * `tidb-monitor.yaml`
        + ```yaml
          <!-- @include: tidb-monitor.yaml -->
          ```
    * `tidb-dashboard.yaml`
        + ```yaml
          <!-- @include: tidb-dashboard.yaml -->
          ```
2. Create a namespace
    ```shell
    kubectl get namespace tidb-cluster > /dev/null 2>&1 \
      || kubectl create namespace tidb-cluster
    ```
3. Apply TiDB Operator CRD and sync
    ```shell
    kubectl -n argocd apply -f tidb-operator-crd.yaml
    argocd app sync argocd/tidb-operator-crd
    argocd app wait argocd/tidb-operator-crd
    ```
4. Apply TiDB Operator and sync
    ```shell
    kubectl -n argocd apply -f tidb-operator.yaml
    argocd app sync argocd/tidb-operator
    argocd app wait argocd/tidb-operator
    ```
5. Apply TiDB cluster and initializer components
    * prepare secret named `basic-tidb-credentials` to store the credential of tidb root user
        + ```shell
          kubectl -n tidb-cluster create secret generic basic-tidb-credentials --from-literal=root=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16 | base64)
          ```
    * apply resources
        + ```shell
          kubectl -n tidb-cluster apply -f tidb-init-sql.configmap.yaml
          kubectl -n tidb-cluster apply -f tidb-cluster.yaml
          kubectl -n tidb-cluster apply -f tidb-initializer.yaml
          ```
6. Apply TiDB monitoring components
    * prepare secret named `basic-grafana-credentials` to store the credential of grafana admin user
        + ```shell
          kubectl -n tidb-cluster create secret generic basic-grafana-credentials \
            --from-literal=username=admin \
            --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
          ```
    * apply resources
        + ```shell
          kubectl -n tidb-cluster apply -f tidb-monitor.yaml
          ```
7. Apply TiDB Dashboard components
    * apply resources
        + ```shell
          kubectl -n tidb-cluster apply -f tidb-dashboard.yaml
          ```

## simple checks

1. running querys by tidb(mysql interface)
    * ```shell
      kubectl -n tidb-cluster apply -f query.job.yaml
      kubectl -n tidb-cluster wait --for=condition=complete job/mysql-query-job
      kubectl -n tidb-cluster logs -l job-name=mysql-query-job
      ```

## main operations

1. [scale in/out](scale-in-and-out.md)
2. [monitor and alerts](monitor-and-alerts.md)
3. [benchmarks for performance evaluation](benchmarks.md)
4. [backup and restore](backup-and-restore.md)
5. [import and export](import-and-export.md)
6. [cdc](cdc.md)
7. [rbac for mysql interface](rabc-for-mysql.md)

## tests

1. Check the status of the TiDB cluster
    ```shell
    kubectl -n tidb-cluster get pods
    ```

2. Access the TiDB Dashboard
    ```shell
    kubectl -n tidb-cluster port-forward svc/basic-tidb-dashboard-exposed 12333:12333 --address 0.0.0.0
    ```
    Then open `http://localhost:12333` in your browser. Please replace `<tidb-dashboard-service-name>` with the actual TiDB Dashboard service name.

## uninstallation

1. uninstall TiDB cluster
    * ```shell
      kubectl -n tidb-cluster delete -f tidb-cluster.yaml
      kubectl -n tidb-cluster delete -f tidb-initializer.yaml
      kubectl -n tidb-cluster delete -f tidb-monitor.yaml
      kubectl -n tidb-cluster delete -f tidb-dashboard.yaml
      ```
    * ```shell
      kubectl -n tidb-cluster delete secret basic-tidb-credentials
      kubectl -n tidb-cluster delete secret basic-grafana-credentials
      ```
    * ```shell
      # check and delete PVCs created
      kubectl -n tidb-cluster delete pvc -l app.kubernetes.io/managed-by=tidb-operator,app.kubernetes.io/instance=basic
      #kubectl -n tidb-cluster delete pvc -l app.kubernetes.io/managed-by=tidb-operator
      ```
    * ```shell
      kubectl delete namespace tidb-cluster
      ```
2. uninstall TiDB operator
    * ```shell
      kubectl -n argocd delete -f tidb-operator.yaml
      ```
3. uninstall TiDB operator CRDs
    * ```shell
      kubectl -n argocd delete -f tidb-operator-crd.yaml
      ```
