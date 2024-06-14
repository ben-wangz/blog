# flink jdbc connector

## how to run

### with vscode

1. setup clickhouse server
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

* reference: [flink-connectors-jdbc](https://blog.geekcity.tech/articles/kubernetes/flink/jdbc/)
