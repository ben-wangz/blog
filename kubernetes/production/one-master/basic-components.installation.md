# install basic-components

## install ingress-nginx

1. prepare [ingress.nginx.values.yaml](resources/basic-components/ingress.nginx.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "k8s.gcr.io_ingress-nginx_controller_v1.0.3.dim" \
          "k8s.gcr.io_ingress-nginx_kube-webhook-certgen_v1.0.dim" \
          "k8s.gcr.io_defaultbackend-amd64_1.5.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          docker image load -i $IMAGE_FILE
      done
      ```
3. install ingress-nginx with helm
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-ingress-nginx \
          /root/data/charts/ingress-nginx-4.0.5.tgz \
          --values ingress.nginx.values.yaml \
          --atomic
      ```

## install cert-manager

1. prepare [cert.manager.values.yaml](resources/basic-components/cert.manager.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "quay.io_jetstack_cert-manager-controller_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-webhook_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-cainjector_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-ctl_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-acmesolver_v1.5.4.dim" \
          "ghcr.io_devmachine-fr_cert-manager-alidns-webhook_cert-manager-alidns-webhook_0.2.0.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          docker image load -i $IMAGE_FILE
      done
      ```
3. install cert-manager with helm
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-cert-manager \
          /root/data/charts/cert-manager-v1.5.4.tgz \
          --values cert.manager.values.yaml \
          --atomic
      ```
4. prepare [alidns.webhook.values.yaml](resources/basic-components/alidns.webhook.values.yaml.md)
5. make sure permissions added to $YOUR_ACCESS_KEY_ID
6. create secret of `alidns-webhook-secrets`
    * ```shell
      #YOUR_ACCESS_KEY_ID=xxxxxxxxxxx
      #YOUR_ACCESS_KEY_SECRET=yyyyyyyyyyy
      kubectl -n basic-components create secret generic alidns-webhook-secrets \
          --from-literal="access-token=$YOUR_ACCESS_KEY_ID" \
          --from-literal="secret-key=$YOUR_ACCESS_KEY_SECRET"
      ```
7. install `alidns-webhook`
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-alidns-webhook \
          /root/data/charts/alidns-webhook-0.6.0.tgz \
          --values alidns.webhook.values.yaml \
          --atomic
      ```
8. prepare [alidns.webhook.cluster.issuer.yaml](resources/basic-components/alidns.webhook.cluster.issuer.yaml.md)
9. apply cluster issuer
    * ```shell
      kubectl -n basic-components apply -f alidns.webhook.cluster.issuer.yaml
      ```

## install docker-registry

1. prepare [docker.registry.values.yaml](resources/basic-components/docker.registry.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_registry_2.7.1.dim" \
          "docker.io_busybox_1.33.1-uclibc.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          docker image load -i $IMAGE_FILE
      done
      ```
3. install by helm
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-docker-registry \
          /root/data/charts/docker-registry-1.14.0.tgz \
          --values docker.registry.values.yaml \
          --atomic
      ```
4. configure ingress
    * NOTE: ingress in helm chart is not compatible enough for us, we have to install ingress manually
    * prepare [docker.registry.ingress.yaml](resources/basic-components/docker.registry.ingress.yaml.md)
    * apply ingress
        + ```shell
          kubectl -n basic-components apply -f docker.registry.ingress.yaml
          ```
5. wait for certificate named `docker-registry-geekcity-tech-tls` to be ready
    * ```shell
      kubectl -n basic-components get certificate -w
      # check tls secret
      kubectl -n basic-components get secret docker-registry-geekcity-tech-tls
      ```
6. check with docker client
    * ```shell
      IMAGE=busybox:1.33.1-uclibc \
          && DOCKER_REGISTRY_SERVICE=docker.registry.geekcity.tech:32443 \
          && docker pull $IMAGE \
          && docker tag $IMAGE $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && docker push $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && docker image rm $DOCKER_REGISTRY_SERVICE/$IMAGE \
          && docker pull $DOCKER_REGISTRY_SERVICE/$IMAGE
      ```