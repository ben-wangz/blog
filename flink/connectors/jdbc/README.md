# flink jdbc connector

## how to run

### with vscode

1. start clickhouse server
    * default: jdbc:clickhouse://ben:123456@localhost:18123
    * reference: https://blog.geekcity.tech/articles/docker/software/database/clickhouse.html
2. local run with vscode
    * [SinkToJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SinkToJdbc.java)
    * [SourceFromJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SourceFromJdbc.java)

### with flink operator

1. clickhouse server
    * reference: https://blog.geekcity.tech/articles/kubernetes/argocd/database/clickhouse/
2. flink operator
    * reference: https://blog.geekcity.tech/articles/kubernetes/argocd/flink/
3. build image and push to docker hub
    * ```shell
      IMAGE=docker.io/wangz2019/flink-connectors-jdbc-demo:latest
      bash flink/connectors/jdbc/container/build.sh $IMAGE && podman push $IMAGE
      ```
4. deploy flink job
    * prepare [/tmp/flink-job.template.yaml](../../flink-job-template.yaml)
    * generate `/tmp/flink-job.yaml`
        + ```shell
          export IMAGE=docker.io/wangz2019/flink-connectors-jdbc-demo:latest
          cp /tmp/flink-job.template.yaml /tmp/flink-job.yaml \
              && yq eval ".spec.image = env(IMAGE)" -i /tmp/flink-job.yaml \
              && yq eval ".metadata.name = \"flink-connectors-jdbc-demo\"" -i /tmp/flink-job.yaml
          ```
    * apply to k8s
        + ```shell
          kubectl -n flink apply -f /tmp/flink-job.yaml
          ```
