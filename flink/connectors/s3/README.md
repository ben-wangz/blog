# flink jdbc connector

## how to run

1. start s3 server
    * default endpoint: http://localhost:9000
    * default credentials: minioadmin:minioadmin
    * reference: https://blog.geekcity.tech/articles/docker/software/storage/minio.html
2. local run with vscode
    * [SinkToS3WithParquet](src/main/java/tech/geekcity/flink/connectors/s3/SinkToS3WithParquet.java)
    * [SourceFromS3WithParquet](src/main/java/tech/geekcity/flink/connectors/s3/SourceFromS3WithParquet.java)