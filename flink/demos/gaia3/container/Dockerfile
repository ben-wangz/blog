ARG BUILD_IMAGE=docker.io/library/gradle:8.5.0-jdk11-jammy
ARG BASE_IMAGE=docker.io/library/flink:1.19
FROM $BUILD_IMAGE as builder
COPY . /code
WORKDIR /code
RUN gradle :flink:demos:gaia3:buildBinary -P RESULT_MESSAGE_PATH=/tmp/build-binary-result.txt \
    && mv $(cat /tmp/build-binary-result.txt) /tmp/flink-demos-gaia3.jar

FROM $BASE_IMAGE
RUN mkdir /opt/flink/plugins/s3-fs-hadoop \
    && cp /opt/flink/opt/flink-s3-fs-hadoop-1.19.*.jar /opt/flink/plugins/s3-fs-hadoop/
COPY --from=builder /tmp/flink-demos-gaia3.jar /app/flink-application.jar
