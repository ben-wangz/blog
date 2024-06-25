ARG BUILD_IMAGE=docker.io/library/gradle:8.5.0-jdk11-jammy
ARG BASE_IMAGE=docker.io/library/flink:1.19
FROM $BUILD_IMAGE as builder
COPY . /code
WORKDIR /code
RUN gradle :flink:connectors:s3-with-parquet:buildBinary -P RESULT_MESSAGE_PATH=/tmp/build-binary-result.txt \
    && mv $(cat /tmp/build-binary-result.txt) /tmp/flink-connectors-s3-with-parquet-demo.jar

FROM $BASE_IMAGE
COPY --from=builder /tmp/flink-connectors-s3-with-parquet-demo.jar /app/flink-application.jar
