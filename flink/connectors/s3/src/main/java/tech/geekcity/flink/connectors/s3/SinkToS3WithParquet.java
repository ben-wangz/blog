package tech.geekcity.flink.connectors.s3;

import org.apache.commons.lang3.RandomStringUtils;
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
import tech.geekcity.flink.connectors.s3.pojo.Person;

public class SinkToS3WithParquet {
  private static final String ENDPOINT = "http://localhost:9000";
  private static final String ACCESS_KEY = "minioadmin";
  private static final String ACCESS_SECRET = "minioadmin";
  protected static final String BUCKET = "test";
  protected static final String JOB_NAME = "sink-to-s3-with-parquet";

  public static void main(String[] args) throws Exception {
    Configuration pluginConfiguration = new Configuration();
    pluginConfiguration.setString("s3.access-key", ACCESS_KEY);
    pluginConfiguration.setString("s3.secret-key", ACCESS_SECRET);
    pluginConfiguration.setString("s3.endpoint", ENDPOINT);
    pluginConfiguration.setBoolean("s3.path.style.access", Boolean.TRUE);
    FileSystem.initialize(
        pluginConfiguration, PluginUtils.createPluginManagerFromRootFolder(pluginConfiguration));
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    GeneratorFunction<Long, Tuple2<String, Integer>> generatorFunction =
        index -> Tuple2.of(RandomStringUtils.randomAlphabetic(10), index.intValue());
    Long generateCount = 10000L;
    double recordsPerSecond = 100;
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
                    new Path(String.format("s3://%s/%s", BUCKET, JOB_NAME)),
                    AvroParquetWriters.forReflectRecord(Person.class))
                .withRollingPolicy(OnCheckpointRollingPolicy.build())
                .build());
    env.execute(JOB_NAME);
  }
}
