package tech.geekcity.flink.connectors.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcConnectionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class SinkToJdbc {
  protected static final String JOB_NAME = "sink-to-jdbc";
  protected static final String DRIVER_NAME = "com.clickhouse.jdbc.ClickHouseDriver";
  protected static final String URL_TEMPLATE =
      "jdbc:clickhouse://%s:%s@%s:%s/%s?createDatabaseIfNotExist=true";
  private static final Random RANDOM = new Random();
  private static final String SQL = "insert into users(name, age) values(?, ?)";

  public static void main(String[] args) throws Exception {
    String defaultHost =
        Optional.ofNullable(System.getenv("CLICK_HOUSE_HOST"))
            .orElseGet(
                () ->
                    StringUtils.equals("true", System.getenv("DEV_CONTAINER"))
                        ? "host.containers.internal"
                        : "localhost");
    int defaultPort =
        Optional.ofNullable(System.getenv("CLICK_HOUSE_PORT")).map(Integer::parseInt).orElse(18123);
    String defaultUsername =
        Optional.ofNullable(System.getenv("CLICK_HOUSE_USERNAME")).orElse("ben");
    String defaultPassword =
        Optional.ofNullable(System.getenv("CLICK_HOUSE_PASSWORD")).orElse("123456");
    boolean clickHouseClusterMode =
        Optional.ofNullable(System.getenv("CLICK_HOUSE_CLUSTER_MODE"))
            .map(Boolean::parseBoolean)
            .orElse(false);
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String host = parameterTool.get("host", defaultHost);
    int port = parameterTool.getInt("port", defaultPort);
    String username = parameterTool.get("username", defaultUsername);
    String password = parameterTool.get("password", defaultPassword);
    String database = parameterTool.get("database", JOB_NAME.replaceAll("-", "_"));
    String url = String.format(URL_TEMPLATE, username, password, host, port, database);
    initializeTable(url, clickHouseClusterMode);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    Tuple2<String, Integer>[] records =
        IntStream.range(0, 100)
            .mapToObj(
                index ->
                    Tuple2.<String, Integer>of(
                        RandomStringUtils.randomAlphanumeric(8), RANDOM.nextInt(100)))
            .toArray((IntFunction<Tuple2<String, Integer>[]>) Tuple2[]::new);
    env.fromElements(records)
        .addSink(
            JdbcSink.sink(
                SQL,
                (prepareStatement, tuple2) -> {
                  prepareStatement.setString(1, tuple2.f0);
                  prepareStatement.setInt(2, tuple2.f1);
                },
                new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                    .withUrl(url)
                    .withDriverName(DRIVER_NAME)
                    .build()));
    env.execute(JOB_NAME);
  }

  private static void initializeTable(String url, boolean clickHouseClusterMode)
      throws ClassNotFoundException, SQLException {
    String createTableSqlTemplate =
        "create table if not exists users %s(name String, age Int32) "
            + "ENGINE = MergeTree() order by name";
    String createTableSql =
        String.format(createTableSqlTemplate, clickHouseClusterMode ? "on cluster " : "");
    Class.forName(DRIVER_NAME);
    try (Connection connection = DriverManager.getConnection(url)) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSql)) {
        preparedStatement.execute();
      }
    }
  }
}
