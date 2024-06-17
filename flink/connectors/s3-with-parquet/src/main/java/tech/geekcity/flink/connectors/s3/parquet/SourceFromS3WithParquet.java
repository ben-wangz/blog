package tech.geekcity.flink.connectors.s3.parquet;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.plugin.PluginUtils;
import org.apache.flink.formats.parquet.avro.AvroParquetReaders;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import tech.geekcity.flink.connectors.s3.parquet.pojo.Person;

public class SourceFromS3WithParquet {
  private static final String JOB_NAME = "source-from-s3-with-parquet";

  public static void main(String[] args) throws Exception {
    String schema = Optional.ofNullable(System.getenv("S3_SCHEMA")).orElse("http");
    String host =
        Optional.ofNullable(System.getenv("S3_HOST"))
            .orElseGet(
                () ->
                    StringUtils.equals("true", System.getenv("DEV_CONTAINER"))
                        ? "host.containers.internal"
                        : "localhost");
    String port = Optional.ofNullable(System.getenv("S3_PORT")).orElse("9000");
    String endpoint = String.format("%s://%s:%s", schema, host, port);
    String accessKey = Optional.ofNullable(System.getenv("S3_ACCESS_KEY")).orElse("minioadmin");
    String accessSecret =
        Optional.ofNullable(System.getenv("S3_ACCESS_SECRET")).orElse("minioadmin");
    String defaultBucket = Optional.ofNullable(System.getenv("S3_BUCKET")).orElse("test");
    Configuration pluginConfiguration = new Configuration();
    pluginConfiguration.setString("s3.access-key", accessKey);
    pluginConfiguration.setString("s3.secret-key", accessSecret);
    pluginConfiguration.setString("s3.endpoint", endpoint);
    pluginConfiguration.set(
        ConfigOptions.key("s3.path.style.access").booleanType().noDefaultValue(), Boolean.TRUE);
    FileSystem.initialize(
        pluginConfiguration, PluginUtils.createPluginManagerFromRootFolder(pluginConfiguration));
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String bucket = parameterTool.get("app.s3.bucket", defaultBucket);
    String path = parameterTool.get("app.s3.path", SinkToS3WithParquet.JOB_NAME);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    FileSource<Person> fileSource =
        FileSource.forRecordStreamFormat(
                AvroParquetReaders.forReflectRecord(Person.class),
                new Path(String.format("s3://%s/%s", bucket, path)))
            .build();
    env.fromSource(fileSource, WatermarkStrategy.noWatermarks(), "file-source")
        .addSink(new PrintSinkFunction<>("print-sink", false));
    env.execute(JOB_NAME);
  }
}
