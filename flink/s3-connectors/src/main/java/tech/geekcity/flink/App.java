package tech.geekcity.flink;

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
import tech.geekcity.flink.pojo.Person;

public class App {
  public static void main(String[] args) throws Exception {
    Configuration pluginConfiguration = new Configuration();
    pluginConfiguration.setString("s3.access-key", "minioadmin");
    pluginConfiguration.setString("s3.secret-key", "minioadmin");
    pluginConfiguration.setString("s3.endpoint", "http://localhost:9000");
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
    env.fromSource(source, WatermarkStrategy.noWatermarks(), "Generator Source")
        .map(tuple2 -> Person.builder().name(tuple2.f0).age(tuple2.f1).build())
        .sinkTo(
            FileSink.<Person>forBulkFormat(
                    new Path("s3://test/flink-s3"),
                    AvroParquetWriters.forReflectRecord(Person.class))
                .build());
    env.execute("Flink S3 Connector Example");
  }
}
