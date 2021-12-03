# minio

## main usage

* an object storage service

## conceptions

* none

## practise

### pre-requirements

* none

### purpose

* create a kubernetes cluster by kind
* setup ingress-nginx
* install minio

### do it

1. [create local cluster for testing](../basic/local.cluster.for.testing.md)
2. install ingress nginx
    * prepare [ingress.nginx.values.yaml](../basic/resources/ingress.nginx.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "k8s.gcr.io/ingress-nginx/controller:v1.0.3" "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0"
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * install with helm
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace basic-components \
              my-ingress-nginx \
              ingress-nginx \
              --version 4.0.5 \
              --repo https://kubernetes.github.io/ingress-nginx \
              --values ingress.nginx.values.yaml \
              --atomic
          ```
3. install minio
    * prepare [minio.values.yaml](resources/minio.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "docker.io/bitnami/minio:2021.9.18-debian-10-r0" \
              "docker.io/bitnami/minio-client:2021.9.2-debian-10-r17" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r198" 
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * install with helm
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace application \
              my-minio \
              minio \
              --version 8.1.2 \
              --repo https://charts.bitnami.com/bitnami \
              --values minio.values.yaml \
              --atomic
          ```
4. add the mapping of `$HOST` and `web.minio.local` to `/etc/hosts`
5. upload file with `minio-client`
    * find access key and secret key
        + ```shell
          ACCESS_KEY=$(\
              ./bin/kubectl get secret \
              --namespace application \
              my-minio \
              -o jsonpath="{.data.access-key}" \
              | base64 --decode \
          )
          SECRET_KEY=$(\
              ./bin/kubectl get secret \
              --namespace application \
              my-minio \
              -o jsonpath="{.data.secret-key}" \
              | base64 --decode\
          )
          echo $ACCESS_KEY
          echo $SECRET_KEY
          ```
    * create a pod to use `minio-client`
        + ```shell
          ./bin/kubectl run --namespace application my-minio-tool \
              --env MINIO_SERVER_ACCESS_KEY=$ACCESS_KEY \
              --env MINIO_SERVER_SECRET_KEY=$SECRET_KEY \
              --env MINIO_SERVER_HOST=my-minio \
              --env MINIO_SERVER_PORT=9000 \
              --image localhost:5000/docker.io/bitnami/minio-client:2021.9.2-debian-10-r17 \
              --command tail -- -f /etc/hosts
          ```
    * add config for server
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc config host add minio http://$MINIO_SERVER_HOST:$MINIO_SERVER_PORT $MINIO_SERVER_ACCESS_KEY $MINIO_SERVER_SECRET_KEY'
          ```
    * list buckets
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc ls minio'
          ```
    * create bucket
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc mb minio/bucket-from-client-a && mc mb minio/bucket-from-client-b'
          ```
    * delete bucket
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc rb minio/bucket-from-client-a'
          ```
    * list file in bucket
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc ls minio/bucket-from-client-b'
          ```
    * add file to bucket
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc cp /etc/hosts minio/bucket-from-client-b/hosts && mc cp /etc/hosts minio/bucket-from-client-b/hosts-copy'
          ```
    * delete file from bucket
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc rm minio/bucket-from-client-b/hosts'
          ```
    * cat file content
        + ```shell
          ./bin/kubectl -n application exec -it my-minio-tool \
              -- bash -c 'mc cat minio/bucket-from-client-b/hosts-copy'
          ```
6. visit `http://web.minio.local`