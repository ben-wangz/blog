# minio

## main usage

* an object storage service

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `minio`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="minio.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_minio_2021.9.18-debian-10-r0.dim" \
          "docker.io_bitnami_minio-client_2021.9.2-debian-10-r17.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r198.dim"
      ```
3. install `minio`
    * prepare [minio.values.yaml](resources/minio/minio.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/minio:2021.9.18-debian-10-r0" \
              "docker.io/bitnami/minio-client:2021.9.2-debian-10-r17" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r198"
          ```
    * install with helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-minio \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/minio-8.1.2.tgz \
              --values minio.values.yaml \
              --atomic
          ```
4. install `minio-tool`
    * prepare [minio.tool.yaml](resources/minio/minio.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f minio.tool.yaml
      ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: web.minio.local' https://localhost
      ```
2. test with `minio-tool`
    * find `POD_NAME`
        + ```shell
          POD_NAME=$(kubectl get pod -n application \
              -l "app.kubernetes.io/name=minio-tool" \
              -o jsonpath="{.items[0].metadata.name}")
          ```
    * add config for server
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc config host add minio http://$MINIO_SERVER_HOST:$MINIO_SERVER_PORT $MINIO_SERVER_ACCESS_KEY $MINIO_SERVER_SECRET_KEY'
          ```
    * list buckets
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc ls minio'
          ```
    * create bucket
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc mb minio/bucket-from-client-a && mc mb minio/bucket-from-client-b'
          ```
    * delete bucket
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc rb minio/bucket-from-client-a'
          ```
    * list file in bucket
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc ls minio/bucket-from-client-b'
          ```
    * add file to bucket
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc cp /etc/hosts minio/bucket-from-client-b/hosts && mc cp /etc/hosts minio/bucket-from-client-b/hosts-copy'
          ```
    * delete file from bucket
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc rm minio/bucket-from-client-b/hosts'
          ```
    * cat file content
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c 'mc cat minio/bucket-from-client-b/hosts-copy'
          ```
3. test with broswer
    * add the mapping of `$HOST` and `web.minio.local` to `/etc/hosts`
        + ```shell
          cat $QEMU_HOST_IP web.minio.local > /etc/hosts
          ```
    * extract `access-key` and `secret-key`
        + ```shell
          kubectl get secret --namespace application my-minio -o jsonpath="{.data.access-key}" | base64 --decode && echo 
          kubectl get secret --namespace application my-minio -o jsonpath="{.data.secret-key}" | base64 --decode && echo 
          ```
    * visit `http://web.minio.local`
        + login with `access-key` and `secret-key` extracted

## uninstallation

1. uninstall `minio-tool`
    * ```shell
      kubectl -n application delete -f minio.tool.yaml
      ```
2. uninstall `minio`
    * ```shell
      helm -n application uninstall my-minio \
          && kubectl -n application delete pvc my-minio
      ```
