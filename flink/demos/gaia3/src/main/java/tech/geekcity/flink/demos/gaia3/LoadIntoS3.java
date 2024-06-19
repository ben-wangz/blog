package tech.geekcity.flink.demos.gaia3;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.ConfigOptions;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.geekcity.flink.demos.gaia3.pojo.Gaia3Record;

public class LoadIntoS3 {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadIntoS3.class);
  protected static final String JOB_NAME = "load-into-s3";

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
    String defaultBucket = Optional.ofNullable(System.getenv("S3_BUCKET")).orElse("flink-demos-gaia3");
    long defaultCheckpointInterval =
        Optional.ofNullable(System.getenv("CHECKPOINT_INTERVAL"))
            .map(Long::parseLong)
            .orElse(30000L);
    int defaultUrlLimit =
        Optional.ofNullable(System.getenv("URL_LIMIT")).map(Integer::parseInt).orElse(3);
    String defaultGaia3DirectoryUrl =
        Optional.ofNullable(System.getenv("GAIA3_DIRECTORY_URL"))
            .orElse("https://cdn.gea.esac.esa.int/Gaia/gdr3/gaia_source/");
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
    String path = parameterTool.get("app.s3.path", LoadIntoS3.JOB_NAME);
    long checkpointInterval =
        parameterTool.getLong("app.checkpoint.interval", defaultCheckpointInterval);
    int urlLimit = parameterTool.getInt("app.url.limit", defaultUrlLimit);
    String gaia3DirectoryUrl =
        parameterTool.get("app.gaia3.directory.url", defaultGaia3DirectoryUrl);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(
            ParameterTool.fromMap(
                    Stream.concat(
                            pluginConfiguration.toMap().entrySet().stream(),
                            parameterTool.toMap().entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .getConfiguration());
    env.getCheckpointConfig().setCheckpointInterval(checkpointInterval);
    Document document = Jsoup.connect(gaia3DirectoryUrl).get();
    List<String> dataUrls =
        document.select("a").stream()
            .map(element -> element.absUrl("href"))
            .filter(href -> href.endsWith(".csv.gz"))
            .limit(urlLimit)
            .collect(Collectors.toList());
    LOGGER.info("dataUrls will be added to source: {}", dataUrls);
    GeneratorFunction<Long, String> generatorFunction = index -> dataUrls.get(index.intValue());
    Long generateCount = Long.valueOf(dataUrls.size());
    DataGeneratorSource<String> source =
        new DataGeneratorSource<String>(generatorFunction, generateCount, Types.STRING);
    env.fromSource(source, WatermarkStrategy.noWatermarks(), "data-generator-source")
        .flatMap(UrlToContentLines.builder().sslVerify(true).contentGzip(true).build())
        .filter(line -> !line.startsWith("#") && !line.startsWith("solution_id"))
        .map(Gaia3Record::fromLine)
        .sinkTo(
            FileSink.<Gaia3Record>forBulkFormat(
                    new Path(String.format("s3://%s/%s", bucket, path)),
                    AvroParquetWriters.forReflectRecord(Gaia3Record.class))
                .withRollingPolicy(OnCheckpointRollingPolicy.build())
                .build());
    env.execute(JOB_NAME);
  }
}
