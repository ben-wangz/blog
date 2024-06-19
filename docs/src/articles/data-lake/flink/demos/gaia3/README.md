# s3-with-parquet connector

## introduction

* this is a demo for reading and writing data from/to s3 with parquet format

## source code

* https://github.com/ben-wangz/blog/tree/main/flink/demos/gaia3

## how to run with flink-kubernetes-operator

1. setup minio server
    * reference: [minio](../../../kubernetes/argocd/storage/minio/minio.md)
2. setup flink kubernetes operator
    * reference: [flink-operator](../../../kubernetes/argocd/flink/README.md)
3. copy secret `minio-credentials` from `storage` namespace to `flink` namespace
    * ```shell
      kubectl -n storage get secret minio-credentials -o json \
          | jq 'del(.metadata["namespace","creationTimestamp","resourceVersion","selfLink","uid"])' \
          | kubectl -n flink apply -f -
      ```
4. build image and push to docker hub
    * ```shell
      #REGISTRY_USERNAME=your-registry-username
      #REGISTRY_PASSWORD=your-registry-password
      IMAGE=docker.io/wangz2019/flink-demos-gaia3:latest
      bash flink/demos/gaia3/container/build.sh $IMAGE \
          && podman login -u $REGISTRY_USERNAME -p $REGISTRY_PASSWORD ${REGISTRY:-docker.io} \
          && podman push $IMAGE
      ```
5. create bucket named `flink-demos-gaia3`
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc mb --ignore-existing minio/flink-demos-gaia3"
      ```
5. deploy flink job
    * prepare `flink-job.template.yaml`
        + ```yaml
          <!-- @include: flink-job-template.yaml -->
          ```
    * generate `flink-job.yaml`
        + ```shell
          IMAGE=docker.io/wangz2019/flink-demos-gaia3:latest
          ENTRY_CLASS=tech.geekcity.flink.demos.gaia3.LoadIntoS3
          cp flink-job.template.yaml flink-job.yaml \
              && yq eval ".spec.image = \"$IMAGE\"" -i flink-job.yaml \
              && yq eval ".spec.job.entryClass = \"$ENTRY_CLASS\"" -i flink-job.yaml
          ```
        + add s3 configuration to flink conf
            * reference: [problem of s3 fs filesystem hadoop](../../connectors/s3-with-parquet/README.md#problems)
            * ```shell
              S3_ENDPOINT=http://minio.storage:9000
              S3_ACCESS_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d)
              S3_SECRET_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
              yq eval ".spec.flinkConfiguration.\"s3.endpoint\" = \"$S3_ENDPOINT\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"s3.access-key\" = \"$S3_ACCESS_KEY\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"s3.secret-key\" = \"$S3_SECRET_KEY\"" -i flink-job.yaml
              ```
    * apply to k8s
        + ```shell
          kubectl -n flink apply -f flink-job.yaml
          ```
6. check with mc client
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          --env TZ=Asia/Shanghai \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc ls minio/flink-demos-gaia3/load-into-s3/$(date '+%Y-%m-%d--%H')"
      ```
    * ```shell
      #PART_FILENAME=part-2a79ffe3-76e2-4e6f-9306-4a3ede731af1-0
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          --env TZ=Asia/Shanghai \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc head --lines 20 minio/flink-demos-gaia3/load-into-s3/$(date '+%Y-%m-%d--%H')/$PART_FILENAME"
      ```
