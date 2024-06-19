package tech.geekcity.flink.demos.gaia3.pojo;

import com.google.common.base.Splitter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Gaia3Record {
  private static final Splitter SPLITTER = Splitter.on(",").limit(8).trimResults();

  public static Gaia3Record fromLine(String line) {
    List<String> fieldList =
        StreamSupport.stream(SPLITTER.split(line).spliterator(), false)
            .collect(Collectors.toList());
    return Gaia3Record.builder()
        .solutionId(Long.parseLong(fieldList.get(0)))
        .designation(fieldList.get(1))
        .sourceId(Long.parseLong(fieldList.get(2)))
        .randomIndex(Long.parseLong(fieldList.get(3)))
        .refEpoch(Double.parseDouble(fieldList.get(4)))
        .ra(Double.parseDouble(fieldList.get(5)))
        .raError(Double.parseDouble(fieldList.get(6)))
        .build();
  }

  private Long solutionId;
  private String designation;
  private Long sourceId;
  private Long randomIndex;
  private Double refEpoch;
  private Double ra;
  private Double raError;
}
