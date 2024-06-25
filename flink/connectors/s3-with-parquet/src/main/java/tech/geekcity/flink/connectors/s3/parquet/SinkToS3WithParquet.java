package tech.geekcity.flink.connectors.s3.parquet;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.connector.datagen.source.GeneratorFunction;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.core.plugin.PluginUtils;
import org.apache.flink.formats.parquet.avro.AvroParquetWriters;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;
import tech.geekcity.flink.connectors.s3.parquet.pojo.Person;

public class SinkToS3WithParquet {
  protected static final String JOB_NAME = "sink-to-s3-with-parquet";
  private static final Random RANDOM = new Random();

  public static void main(String[] args) throws Exception {
    String schema = Optional.ofNullable(System.getenv("S3_SCHEMA")).orElse("http");
    String host = Optional.ofNullable(System.getenv("S3_HOST")).orElse("host.containers.internal");
    String port = Optional.ofNullable(System.getenv("S3_PORT")).orElse("9000");
    String endpoint = String.format("%s://%s:%s", schema, host, port);
    String accessKey = Optional.ofNullable(System.getenv("S3_ACCESS_KEY")).orElse("minioadmin");
    String accessSecret =
        Optional.ofNullable(System.getenv("S3_ACCESS_SECRET")).orElse("minioadmin");
    String defaultBucket = Optional.ofNullable(System.getenv("S3_BUCKET")).orElse("test");
    long defaultCheckpointInterval =
        Optional.ofNullable(System.getenv("CHECKPOINT_INTERVAL"))
            .map(Long::parseLong)
            .orElse(10000L);
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String bucket = parameterTool.get("app.s3.bucket", defaultBucket);
    String path = parameterTool.get("app.s3.path", SinkToS3WithParquet.JOB_NAME);
    long checkpointInterval =
        parameterTool.getLong("app.checkpoint.interval", defaultCheckpointInterval);
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
        StreamExecutionEnvironment.getExecutionEnvironment(flinkConfiguration);
    env.getCheckpointConfig().setCheckpointInterval(checkpointInterval);
    GeneratorFunction<Long, Tuple2<String, Integer>> generatorFunction =
        index -> Tuple2.of(RandomStringUtils.randomAlphabetic(10), RANDOM.nextInt(100));
    Long generateCount = 10000L;
    double recordsPerSecond = 1000;
    DataGeneratorSource<Tuple2<String, Integer>> source =
        new DataGeneratorSource<Tuple2<String, Integer>>(
            generatorFunction,
            generateCount,
            RateLimiterStrategy.perSecond(recordsPerSecond),
            Types.TUPLE(Types.STRING, Types.INT));
    env.fromSource(source, WatermarkStrategy.noWatermarks(), "data-generator-source")
        .map(tuple2 -> Person.builder().name(tuple2.f0).age(tuple2.f1).build())
        .sinkTo(
            FileSink.<Person>forBulkFormat(
                    new Path(String.format("s3://%s/%s", bucket, path)),
                    AvroParquetWriters.forReflectRecord(Person.class))
                .withRollingPolicy(OnCheckpointRollingPolicy.build())
                .build());
    env.execute(JOB_NAME);
  }
}
