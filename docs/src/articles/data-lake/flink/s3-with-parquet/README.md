# s3-with-parquet connector

## introduction

* this is a demo for reading and writing data from/to s3 with parquet format

## source code

* https://github.com/ben-wangz/blog/tree/main/flink/connectors/s3/parquet

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
      IMAGE=docker.io/wangz2019/flink-connectors-s3-with-parquet-demo:latest
      bash flink/connectors/s3-with-parquet/container/build.sh $IMAGE \
          && podman login -u $REGISTRY_USERNAME -p $REGISTRY_PASSWORD ${REGISTRY:-docker.io} \
          && podman push $IMAGE
      ```
5. deploy flink job
    * prepare `flink-job.template.yaml`
        + ```yaml
          <!-- @include: flink-job-template.yaml -->
          ```
    * generate `flink-job.yaml`
        + ```shell
          IMAGE=docker.io/wangz2019/flink-connectors-s3-with-parquet-demo:latest
          #ENTRY_CLASS=tech.geekcity.flink.connectors.s3/parquet.SourceFromS3WithParquet
          ENTRY_CLASS=tech.geekcity.flink.connectors.s3/parquet.SinkToS3WithParquet
          cp flink-job.template.yaml flink-job.yaml \
              && yq eval ".spec.image = \"$IMAGE\"" -i flink-job.yaml \
              && yq eval ".spec.job.entryClass = \"$ENTRY_CLASS\"" -i flink-job.yaml
          ```
    * apply to k8s
        + ```shell
          kubectl -n flink apply -f flink-job.yaml
          ```
6. check data in clickhouse
    * ```shell
      curl -k "https://admin:${PASSWORD}@clickhouse.dev.geekcity.tech:32443/?database=sink_to_jdbc" --data 'select * from users limit 10'
      ```