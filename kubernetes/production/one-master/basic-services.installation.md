# install basic services

1. create pvc named `resource-nginx-pvc`
    * prepare [resource.nginx.pvc.yaml](resources/resource.nginx.pvc.yaml.md)
    * ```shell
      ./bin/kubectl -n application apply -f resource.nginx.pvc.yaml
      ```
2. prepare [resource.nginx.values.yaml](resources/resource.nginx.values.yaml.md)
3. prepare images
    * ```shell
      for IMAGE in "docker.io/bitnami/nginx:1.21.3-debian-10-r29" \
          "docker.io/bitnami/git:2.33.0-debian-10-r53" \
          "docker.io/busybox:1.33.1-uclibc"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
4. install by helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace application \
          resource-nginx \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
          --values resource.nginx.values.yaml \
          --atomic
      ```
5. cp file into `resource.nginx`
    * ```shell
      POD_NAME=$(./bin/kubectl get pod -n application \
          -l "app.kubernetes.io/name=nginx,app.kubernetes.io/instance=resource-nginx" \
          -o jsonpath="{.items[0].metadata.name}") \
          && ./bin/kubectl -n application cp -c busybox /etc/fstab $POD_NAME:/root/data/
      ```
6. manage files
    * ```shell
      POD_NAME=$(./bin/kubectl get pod -n application \
          -l "app.kubernetes.io/name=nginx,app.kubernetes.io/instance=resource-nginx" \
          -o jsonpath="{.items[0].metadata.name}") \
          && ./bin/kubectl -n application exec -it $POD_NAME -c busybox -- sh
      ```