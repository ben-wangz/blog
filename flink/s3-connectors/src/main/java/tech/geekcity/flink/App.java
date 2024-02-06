package tech.geekcity.flink;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class App {
  public static void main(String[] args) {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    Configuration configuration = new Configuration();
    configuration.setString("s3.access-key", "conti");
    configuration.setString("s3.secret-key", "Conti@1234");
    configuration.setString("s3.endpoint", "http://api-minio-dev.lab.zjvis.net:32080");
    configuration.setString("allowed-fallback-filesystems", "oss");
    configuration.setBoolean("s3.path.style.access", Boolean.TRUE);
  }
}
