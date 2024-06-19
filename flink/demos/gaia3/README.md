# flink jdbc connector

## how to run

1. setup s3 server
    * default endpoint: http://localhost:9000
    * default credentials: minioadmin:minioadmin
    * reference: https://blog.geekcity.tech/articles/docker/software/storage/minio.html
2. prepare minio bucket
    * ```shell
        podman run --rm \
            --entrypoint bash \
            -it docker.io/minio/mc:latest \
            -c "mc alias set minio http://host.containers.internal:9000 minioadmin minioadmin \
                && mc mb --ignore-existing minio/flink-demos-gaia3"
      ```
2. local run with vscode
    * [SinkToS3WithParquet](src/main/java/tech/geekcity/flink/demos/gaia3/SinkToS3WithParquet.java)
    * [SourceFromS3WithParquet](src/main/java/tech/geekcity/flink/demos/gaia3/SourceFromS3WithParquet.java)
3. check with mc client
    * ```shell
      podman run --rm \
          --entrypoint bash \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://host.containers.internal:9000 minioadmin minioadmin \
              && mc ls minio/flink-demos-gaia3/load-into-s3/$(date '+%Y-%m-%d--%H')"
      ```
    * ```shell
      #PART_FILENAME=part-2a79ffe3-76e2-4e6f-9306-4a3ede731af1-0
      podman run --rm \
          --entrypoint bash \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://host.containers.internal:9000 minioadmin minioadmin \
              && mc head --lines 20 minio/flink-demos-gaia3/load-into-s3/$(date '+%Y-%m-%d--%H')/$PART_FILENAME"
      ```

### with flink operator

* reference: [flink-connectors-jdbc](https://blog.geekcity.tech/articles/data-lake/flink/demos/gaia3/)
