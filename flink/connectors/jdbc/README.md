# flink jdbc connector

## how to run

1. start clickhouse server
    * default: jdbc:clickhouse://ben:123456@localhost:18123
    * reference: https://blog.geekcity.tech/articles/docker/software/database/clickhouse.html
2. local run with vscode
    * [SinkToJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SinkToJdbc.java)
    * [SourceFromJdbc](src/main/java/tech/geekcity/flink/connectors/jdbc/SourceFromJdbc.java)