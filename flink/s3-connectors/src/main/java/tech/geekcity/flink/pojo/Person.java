package tech.geekcity.flink.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Person {
  private String name;
  private int age;
}
