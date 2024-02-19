package tech.geekcity.flink.connectors.s3;

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
import tech.geekcity.flink.connectors.s3.pojo.Person;

public class SourceFromS3WithParquet {
  private static final String S3_BUCKET = "app.source.s3.bucket";
  private static final String S3_PATH = "app.source.s3.path";
  private static final String ENDPOINT = "http://localhost:9000";
  private static final String ACCESS_KEY = "minioadmin";
  private static final String ACCESS_SECRET = "minioadmin";
  private static final String JOB_NAME = "source-from-s3-with-parquet";

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
    String s3Bucket = parameterTool.get(S3_BUCKET, SinkToS3WithParquet.BUCKET);
    String s3Path = parameterTool.get(S3_PATH, SinkToS3WithParquet.JOB_NAME);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    FileSource<Person> fileSource =
        FileSource.forRecordStreamFormat(
                AvroParquetReaders.forReflectRecord(Person.class),
                new Path(String.format("s3://%s/%s", s3Bucket, s3Path)))
            .build();
    env.fromSource(fileSource, WatermarkStrategy.noWatermarks(), "file-source")
        .addSink(new PrintSinkFunction<>("print-sink", false));
    env.execute(JOB_NAME);
  }
}
