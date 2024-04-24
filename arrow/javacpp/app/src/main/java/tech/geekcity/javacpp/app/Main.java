package tech.geekcity.javacpp.app;

import org.bytedeco.javacpp.Pointer;
import tech.geekcity.javacpp.geekmath.Simple;

public class Main extends Pointer {
  public static void main(String[] args) {
    try (Simple simple = new Simple()) {
      System.out.println(String.format("1 + 2 = %s", simple.add(1, 2)));
    }
  }
}
