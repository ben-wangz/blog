package tech.geekcity.flink.connectors.s3.multiple.parquet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.hadoop.util.HadoopOutputFile;

public class Main {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void main(String[] args) throws IOException {
    String schemaJson =
        OBJECT_MAPPER.writeValueAsString(
            Map.<String, Object>of(
                "type", "record",
                "name", "Person",
                "fields",
                    List.of(
                        Map.<String, String>of(
                            "name", "name",
                            "type", "string"),
                        Map.<String, String>of(
                            "name", "age",
                            "type", "int"),
                        new HashMap<String, Object>() {
                          {
                            put("name", "email");
                            put("type", List.of("null", "string"));
                            put("default", null);
                          }
                        })));
    Schema schema = new Schema.Parser().parse(schemaJson);
    Configuration conf = new Configuration();
    conf.set("fs.s3a.endpoint", "http://host.containers.internal:9000");
    conf.set("fs.s3a.access.key", "minioadmin");
    conf.set("fs.s3a.secret.key", "minioadmin");
    conf.set("fs.s3a.path.style.access", "true");
    Path path = new Path("s3a://test/parquet/person.parquet");
    try (ParquetWriter<GenericRecord> parquetWriter =
        AvroParquetWriter.<GenericRecord>builder(HadoopOutputFile.fromPath(path, conf))
            .withSchema(schema)
            .build()) {
      GenericRecord record = new GenericData.Record(schema);
      record.put("name", "foo");
      record.put("age", 42);
      record.put("email", "ben.wangz@foxmail.com");
      parquetWriter.write(record);
    }
    try (ParquetReader<GenericRecord> parquetReader =
        AvroParquetReader.<GenericRecord>builder(HadoopInputFile.fromPath(path, conf)).build()) {
      GenericRecord record = parquetReader.read();
      System.out.println(record);
    }
  }
}
