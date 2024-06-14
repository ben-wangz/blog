package tech.geekcity.flink.connectors.jdbc;

import java.io.Serializable;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.jdbc.JdbcInputFormat;
import org.apache.flink.connector.jdbc.split.JdbcGenericParameterValuesProvider;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.types.Row;

public class SourceFromJdbc {
  private static final String JOB_NAME = "source-from-jdbc";
  private static final String SQL = "select * from users where modulo(age, 2) = ?";
  private static final Serializable[][] QUERY_PARAMETERS =
      new Integer[][] {new Integer[] {0}, new Integer[] {1}};

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
    // specify flink configuration from args, e.g., --restPort 8081
    ParameterTool parameterTool = ParameterTool.fromArgs(args);
    String host = parameterTool.get("host", defaultHost);
    int port = parameterTool.getInt("port", defaultPort);
    String username = parameterTool.get("username", defaultUsername);
    String password = parameterTool.get("password", defaultPassword);
    String database = parameterTool.get("database", SinkToJdbc.JOB_NAME.replaceAll("-", "_"));
    String url = String.format(SinkToJdbc.URL_TEMPLATE, username, password, host, port, database);
    StreamExecutionEnvironment env =
        StreamExecutionEnvironment.getExecutionEnvironment(parameterTool.getConfiguration());
    TypeInformation<?>[] fieldTypes =
        new TypeInformation<?>[] {BasicTypeInfo.STRING_TYPE_INFO, BasicTypeInfo.INT_TYPE_INFO};
    RowTypeInfo rowTypeInfo = new RowTypeInfo(fieldTypes);
    JdbcInputFormat jdbcInputFormat =
        JdbcInputFormat.buildJdbcInputFormat()
            .setDrivername(SinkToJdbc.DRIVER_NAME)
            .setDBUrl(url)
            .setUsername(username)
            .setPassword(password)
            .setQuery(SQL)
            .setRowTypeInfo(rowTypeInfo)
            .setParametersProvider(new JdbcGenericParameterValuesProvider(QUERY_PARAMETERS))
            .finish();
    DataStreamSource<Row> source = env.createInput(jdbcInputFormat);
    source.addSink(new PrintSinkFunction<>("print-sink", false));
    env.execute(JOB_NAME);
  }
}
