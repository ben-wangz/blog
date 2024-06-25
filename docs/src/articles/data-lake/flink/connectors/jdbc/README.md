# jdbc connector

## introduction

* this is a demo for reading and writing data from/to jdbc, e.g. ClickHouse

## source code

* https://github.com/ben-wangz/blog/tree/main/flink/connectors/jdbc

## how to run with flink-kubernetes-operator

1. setup clickhouse server
    * reference: [clickhouse](../../../kubernetes/argocd/database/clickhouse/README.md)
2. setup flink kubernetes operator
    * reference: [flink-operator](../../../kubernetes/argocd/flink/README.md)
3. copy secret `clickhouse-admin-credentials` from `database` namespace to `flink` namespace
    * ```shell
      kubectl -n database get secret clickhouse-admin-credentials -o json \
          | jq 'del(.metadata["namespace","creationTimestamp","resourceVersion","selfLink","uid"])' \
          | kubectl -n flink apply -f -
      ```
4. build image and push to docker hub
    * ```shell
      #REGISTRY_USERNAME=your-registry-username
      #REGISTRY_PASSWORD=your-registry-password
      IMAGE=docker.io/wangz2019/flink-connectors-jdbc-demo:latest
      bash flink/connectors/jdbc/container/build.sh $IMAGE \
          && podman login -u $REGISTRY_USERNAME -p $REGISTRY_PASSWORD ${REGISTRY:-docker.io} \
          && podman push $IMAGE
      ```
5. deploy flink job
    * prepare `flink-job.template.yaml`
        + ```yaml
          <!-- @include: flink-job.template.yaml -->
          ```
    * generate `flink-job.yaml`
        + ```shell
          IMAGE=docker.io/wangz2019/flink-connectors-jdbc-demo:latest
          #ENTRY_CLASS=tech.geekcity.flink.connectors.jdbc.SourceFromJdbc
          ENTRY_CLASS=tech.geekcity.flink.connectors.jdbc.SinkToJdbc
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