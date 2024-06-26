# s3-with-parquet connector

## introduction

* this is a demo for reading and writing data from/to s3 with parquet format

## source code

* https://github.com/ben-wangz/blog/tree/main/flink/connectors/s3-with-parquet

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
5. (only for minio) create bucket named `flink-connectors-s3-with-parquet-demo`
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc mb --ignore-existing minio/flink-connectors-s3-with-parquet-demo"
      ```
6. deploy flink job
    * prepare `flink-job.template.yaml`
        + ```yaml
          <!-- @include: flink-job.template.yaml -->
          ```
    * generate `flink-job.yaml`
        + ::: code-tabs#shell
          @tab sink
          ```shell
          IMAGE=docker.io/wangz2019/flink-connectors-s3-with-parquet-demo:latest
          ENTRY_CLASS=tech.geekcity.flink.connectors.s3.parquet.SinkToS3WithParquet
          cp flink-job.template.yaml flink-job.yaml \
              && yq eval ".spec.image = \"$IMAGE\"" -i flink-job.yaml \
              && yq eval ".spec.job.entryClass = \"$ENTRY_CLASS\"" -i flink-job.yaml
          ```
          @tab source
          ```shell
          IMAGE=docker.io/wangz2019/flink-connectors-s3-with-parquet-demo:latest
          ENTRY_CLASS=tech.geekcity.flink.connectors.s3.parquet.SourceFromS3WithParquet
          cp flink-job.template.yaml flink-job.yaml \
              && yq eval ".spec.image = \"$IMAGE\"" -i flink-job.yaml \
              && yq eval ".spec.job.entryClass = \"$ENTRY_CLASS\"" -i flink-job.yaml
          ```
          :::
        + add s3 configuration to flink conf
            * reference: [problem of s3 fs filesystem hadoop](#problems)
            * ::: code-tabs#shell
              @tab minio
              ```shell
              S3_ENDPOINT=http://minio.storage:9000
              S3_ACCESS_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d)
              S3_SECRET_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
              ENABLE_BUILT_IN_PLUGINS=flink-s3-fs-hadoop-1.19.0.jar
              yq eval ".spec.podTemplate.spec.containers[0] |= select(.name == \"flink-main-container\").env[] |= select(.name == \"ENABLE_BUILT_IN_PLUGINS\").value = \"$ENABLE_BUILT_IN_PLUGINS\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"s3.endpoint\" = \"$S3_ENDPOINT\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"s3.access-key\" = \"$S3_ACCESS_KEY\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"s3.secret-key\" = \"$S3_SECRET_KEY\"" -i flink-job.yaml
              ```
              @tab oss
              ```shell
              OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
              #OSS_ACCESS_KEY=your-oss-access-key
              #OSS_SECRET_KEY=your-oss-secret-key
              OSS_BUCKET=geekcity
              OSS_PATH=projects/blog/flink/connectors/s3-with-parquet
              ENABLE_BUILT_IN_PLUGINS=flink-oss-fs-hadoop-1.19.0.jar
              yq eval ".spec.podTemplate.spec.containers[0] |= select(.name == \"flink-main-container\").env[] |= select(.name == \"ENABLE_BUILT_IN_PLUGINS\").value = \"$ENABLE_BUILT_IN_PLUGINS\"" -i flink-job.yaml \
                  && yq eval ".spec.podTemplate.spec.containers[0] |= select(.name == \"flink-main-container\").env[] |= select(.name == \"S3_BUCKET\").value = \"$OSS_BUCKET\"" -i flink-job.yaml \
                  && yq eval ".spec.podTemplate.spec.containers[0] |= select(.name == \"flink-main-container\").env[] |= select(.name == \"S3_PATH\").value = \"$OSS_PATH\"" -i flink-job.yaml \
                  && yq eval ".spec.podTemplate.spec.containers[0] |= select(.name == \"flink-main-container\").env[] |= select(.name == \"FILESYSTEM_SCHEMA\").value = \"oss\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"fs.oss.endpoint\" = \"$OSS_ENDPOINT\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"fs.oss.accessKeyId\" = \"$OSS_ACCESS_KEY\"" -i flink-job.yaml \
                  && yq eval ".spec.flinkConfiguration.\"fs.oss.accessKeySecret\" = \"$OSS_SECRET_KEY\"" -i flink-job.yaml
              ```
              :::
    * apply to k8s
        + ```shell
          kubectl -n flink apply -f flink-job.yaml
          ```
7. (only for minio) check with mc client
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          --env TZ=Asia/Shanghai \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc ls minio/flink-connectors-s3-with-parquet-demo/sink-to-s3-with-parquet/$(date '+%Y-%m-%d--%H')"
      ```
    * ```shell
      #PART_FILENAME=part-2a79ffe3-76e2-4e6f-9306-4a3ede731af1-0
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          --env TZ=Asia/Shanghai \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc head --lines 20 minio/flink-connectors-s3-with-parquet-demo/sink-to-s3-with-parquet/$(date '+%Y-%m-%d--%H')/$PART_FILENAME"
      ```

## problems

* there's a problem of flink s3 fs filesystem hadoop
    + workaround: set related s3 configuration in flink conf
    + Filesystem won't be properly initialized for TaskManager
        * ```text
          2024-06-14 12:57:42,972 INFO  org.apache.flink.runtime.executiongraph.ExecutionGraph       [] - Source: data-generator-source -> Map -> Sink: Writer -> Sink: Committer (2/2) (d5f95564f73a98ae321dd9c13d7a2ff5_cbc357ccb763df2852fee8c4fc7d55f2_1_36) switched from RUNNING to FAILED on flink-connectors-s3-with-parquet-demo-taskmanager-1-1 @ 10.244.0.79 (dataPort=46669).
          java.nio.file.AccessDeniedException: sink-to-s3-with-parquet/2024-06-14--12/part-8b60ac7b-df3b-481f-9c53-e8a1f59015ee-0: org.apache.hadoop.fs.s3a.auth.NoAuthWithAWSException: No AWS Credentials provided by DynamicTemporaryAWSCredentialsProvider TemporaryAWSCredentialsProvider SimpleAWSCredentialsProvider EnvironmentVariableCredentialsProvider IAMInstanceCredentialsProvider : com.amazonaws.SdkClientException: Unable to load AWS credentials from environment variables (AWS_ACCESS_KEY_ID (or AWS_ACCESS_KEY) and AWS_SECRET_KEY (or AWS_SECRET_ACCESS_KEY))
                  at org.apache.hadoop.fs.s3a.S3AUtils.translateException(S3AUtils.java:212) ~[?:?]
                  at org.apache.hadoop.fs.s3a.Invoker.once(Invoker.java:119) ~[?:?]
                  at org.apache.hadoop.fs.s3a.Invoker.lambda$retry$4(Invoker.java:322) ~[?:?]
                  at org.apache.hadoop.fs.s3a.Invoker.retryUntranslated(Invoker.java:414) ~[?:?]
                  at org.apache.hadoop.fs.s3a.Invoker.retry(Invoker.java:318) ~[?:?]
                  at org.apache.hadoop.fs.s3a.Invoker.retry(Invoker.java:293) ~[?:?]
                  at org.apache.hadoop.fs.s3a.WriteOperationHelper.retry(WriteOperationHelper.java:208) ~[?:?]
                  at org.apache.hadoop.fs.s3a.WriteOperationHelper.initiateMultiPartUpload(WriteOperationHelper.java:313) ~[?:?]
                  at org.apache.flink.fs.s3hadoop.HadoopS3AccessHelper.startMultiPartUpload(HadoopS3AccessHelper.java:71) ~[?:?]
                  at org.apache.flink.fs.s3.common.writer.RecoverableMultiPartUploadImpl.newUpload(RecoverableMultiPartUploadImpl.java:253) ~[?:?]
                  at org.apache.flink.fs.s3.common.writer.S3RecoverableMultipartUploadFactory.getNewRecoverableUpload(S3RecoverableMultipartUploadFactory.java:68) ~[?:?]
                  at org.apache.flink.fs.s3.common.writer.S3RecoverableWriter.open(S3RecoverableWriter.java:78) ~[?:?]
                  at org.apache.flink.streaming.api.functions.sink.filesystem.OutputStreamBasedPartFileWriter$OutputStreamBasedBucketWriter.openNewInProgressFile(OutputStreamBasedPartFileWriter.java:124) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.api.functions.sink.filesystem.BulkBucketWriter.openNewInProgressFile(BulkBucketWriter.java:36) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.connector.file.sink.writer.FileWriterBucket.rollPartFile(FileWriterBucket.java:261) ~[flink-connector-files-1.17.2.jar:1.17.2]
                  at org.apache.flink.connector.file.sink.writer.FileWriterBucket.write(FileWriterBucket.java:188) ~[flink-connector-files-1.17.2.jar:1.17.2]
                  at org.apache.flink.connector.file.sink.writer.FileWriter.write(FileWriter.java:198) ~[flink-connector-files-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.operators.sink.SinkWriterOperator.processElement(SinkWriterOperator.java:158) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.pushToOperator(CopyingChainingOutput.java:75) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.collect(CopyingChainingOutput.java:50) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.collect(CopyingChainingOutput.java:29) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.api.operators.StreamMap.processElement(StreamMap.java:38) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.pushToOperator(CopyingChainingOutput.java:75) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.collect(CopyingChainingOutput.java:50) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.CopyingChainingOutput.collect(CopyingChainingOutput.java:29) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.SourceOperatorStreamTask$AsyncDataOutputToOutput.emitRecord(SourceOperatorStreamTask.java:309) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.api.operators.source.SourceOutputWithWatermarks.collect(SourceOutputWithWatermarks.java:110) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.api.operators.source.SourceOutputWithWatermarks.collect(SourceOutputWithWatermarks.java:101) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.api.connector.source.lib.util.IteratorSourceReaderBase.pollNext(IteratorSourceReaderBase.java:111) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.api.connector.source.util.ratelimit.RateLimitedSourceReader.pollNext(RateLimitedSourceReader.java:69) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.api.operators.SourceOperator.emitNext(SourceOperator.java:419) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.io.StreamTaskSourceInput.emitNext(StreamTaskSourceInput.java:68) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.io.StreamOneInputProcessor.processInput(StreamOneInputProcessor.java:65) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.StreamTask.processInput(StreamTask.java:550) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.mailbox.MailboxProcessor.runMailboxLoop(MailboxProcessor.java:231) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.StreamTask.runMailboxLoop(StreamTask.java:839) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.streaming.runtime.tasks.StreamTask.invoke(StreamTask.java:788) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.runtime.taskmanager.Task.runWithSystemExitMonitoring(Task.java:952) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.runtime.taskmanager.Task.restoreAndInvoke(Task.java:931) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.runtime.taskmanager.Task.doRun(Task.java:745) ~[flink-dist-1.17.2.jar:1.17.2]
                  at org.apache.flink.runtime.taskmanager.Task.run(Task.java:562) ~[flink-dist-1.17.2.jar:1.17.2]
                  at java.lang.Thread.run(Unknown Source) ~[?:?]
          ```

