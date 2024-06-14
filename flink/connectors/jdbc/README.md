# flink jdbc connector

## how to run

### with vscode

1. start clickhouse server
    * default: jdbc:clickhouse://ben:123456@localhost:18123
    * reference: https://blog.geekcity.tech/articles/docker/software/database/clickhouse.html
2. local run with vscode
    * [SinkToJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SinkToJdbc.java)
    * [SourceFromJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SourceFromJdbc.java)
3. check data in clickhouse
    * ```shell
      curl 'http://ben:123456@localhost:18123/?database=sink_to_jdbc' --data 'select * from users limit 10'
      ```

### with flink operator

1. clickhouse server
    * reference: https://blog.geekcity.tech/articles/kubernetes/argocd/database/clickhouse/
2. flink operator
    * reference: https://blog.geekcity.tech/articles/kubernetes/argocd/flink/
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
    * prepare [/tmp/flink-job.template.yaml](k8s/flink-job-template.yaml)
    * generate `/tmp/flink-job.yaml`
        + ```shell
          IMAGE=docker.io/wangz2019/flink-connectors-jdbc-demo:latest
          #ENTRY_CLASS=tech.geekcity.flink.connectors.jdbc.SourceFromJdbc
          ENTRY_CLASS=tech.geekcity.flink.connectors.jdbc.SinkToJdbc
          cp /tmp/flink-job.template.yaml /tmp/flink-job.yaml \
              && yq eval ".spec.image = \"$IMAGE\"" -i /tmp/flink-job.yaml \
              && yq eval ".spec.job.entryClass = \"$ENTRY_CLASS\"" -i /tmp/flink-job.yaml
          ```
    * apply to k8s
        + ```shell
          kubectl -n flink apply -f /tmp/flink-job.yaml
          ```
6. check data in clickhouse
    * ```shell
      curl -k "https://admin:${PASSWORD}@clickhouse.dev.geekcity.tech:32443/?database=sink_to_jdbc" --data 'select * from users limit 10'
      ```
