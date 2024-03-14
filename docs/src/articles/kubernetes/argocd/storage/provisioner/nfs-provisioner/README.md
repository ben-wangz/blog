# nfs provisioner

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. nfsv4 server is ready
    * nfs server: nfs.services.geekcity.tech

## installation

1. prepare `nfs-provisioner.yaml`
    * ```yaml
      <!-- @include: nfs-provisioner.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f nfs-provisioner.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/nfs-provisioner
      ```
4. check storage class
    + ```shell
      kubectl get sc
      ```

## references

* https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner
* https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner/blob/master/charts/nfs-subdir-external-provisioner/README.md
