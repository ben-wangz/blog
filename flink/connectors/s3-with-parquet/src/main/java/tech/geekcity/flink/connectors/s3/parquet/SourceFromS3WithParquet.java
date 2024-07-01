package tech.geekcity.flink.connectors.s3.parquet;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
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
    String host = Optional.ofNullable(System.getenv("S3_HOST")).orElse("host.containers.internal");
    String port = Optional.ofNullable(System.getenv("S3_PORT")).orElse("9000");
    String endpoint = String.format("%s://%s:%s", schema, host, port);
    String accessKey = Optional.ofNullable(System.getenv("S3_ACCESS_KEY")).orElse("minioadmin");
    String accessSecret =
        Optional.ofNullable(System.getenv("S3_ACCESS_SECRET")).orElse("minioadmin");
    String defaultBucket = Optional.ofNullable(System.getenv("S3_BUCKET")).orElse("test");
    String defaultPath =
        Optional.ofNullable(System.getenv("S3_PATH")).orElse(SinkToS3WithParquet.JOB_NAME);
    String filesystemSchema = Optional.ofNullable(System.getenv("FILESYSTEM_SCHEMA")).orElse("s3");
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String bucket = parameterTool.get("app.s3.bucket", defaultBucket);
    String path = parameterTool.get("app.s3.path", defaultPath);
    boolean devContainer =
        Optional.ofNullable(System.getenv("DEV_CONTAINER"))
            .map(envValue -> StringUtils.equals("true", envValue))
            .orElse(false);
    Configuration flinkConfiguration =
        devContainer
            ? ParameterTool.fromMap(
                    Stream.concat(
                            ImmutableMap.<String, String>builder()
                                .put("s3.access-key", accessKey)
                                .put("s3.secret-key", accessSecret)
                                .put("s3.endpoint", endpoint)
                                .put("s3.path.style.access", "true")
                                .build()
                                .entrySet()
                                .stream(),
                            parameterTool.toMap().entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .getConfiguration()
            : parameterTool.getConfiguration();
    if (devContainer) {
      FileSystem.initialize(
          flinkConfiguration, PluginUtils.createPluginManagerFromRootFolder(flinkConfiguration));
    }
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    FileSource<Person> fileSource =
        FileSource.forRecordStreamFormat(
                AvroParquetReaders.forReflectRecord(Person.class),
                new Path(String.format("%s://%s/%s", filesystemSchema, bucket, path)))
            .build();
    env.fromSource(fileSource, WatermarkStrategy.noWatermarks(), "file-source")
        .addSink(new PrintSinkFunction<>("print-sink", false));
    env.execute(JOB_NAME);
  }
}
