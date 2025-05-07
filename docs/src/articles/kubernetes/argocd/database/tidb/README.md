# TiDB

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. local storage class is ready

## installation

```shell
kubectl -n argocd apply -f tidb-operator-crd.yaml
argocd app sync argocd/tidb-operator-crd
argocd app wait argocd/tidb-operator-crd

kubectl -n argocd apply -f tidb-operator.yaml
argocd app sync argocd/tidb-operator
argocd app wait argocd/tidb-operator

kubectl get namespace tidb-cluster > /dev/null 2>&1 || kubectl create namespace tidb-cluster
kubectl -n tidb-cluster apply -f tidb-cluster.yaml
kubectl -n tidb-cluster apply -f tidb-dashboard.yaml
kubectl -n tidb-cluster apply -f tidb-monitor.yaml
```