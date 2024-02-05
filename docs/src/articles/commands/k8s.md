# k8s

## completion for bash

```shell
source<(kubectl completionbash)
```

## port forwarding

```shell
kubectl port-forward --address 0.0.0.0 $SERVICE_OR_POD 8080:80 # local:pod
```

## update certs

```shell
kubeadm alpha certs renew all
docker ps | grep -v pause | grep -E "etcd|scheduler|controller|apiserver" | awk '{print $1}' | awk '{print "docker","restart",$1}' | bash
cp /etc/kubernetes/admin.conf ~/.kube/config
```

## extract podCIDR
```shell
kubectl get nodes -o jsonpath='{.items[*].spec.podCIDR}'
```

## delete error pods

* with awk
    + ```shell
      kubectl get pods --all-namespaces | grep -E "Error|CrashLoopBackOff" | awk '{print "kubectl","-n",$1,"delete","pod",$2}' | bash
      ```
* with kubectl only
    + ```shell
      kubectl -n argocd delete pods --field-selector status.phase=Failed
      ```