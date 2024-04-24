package tech.geekcity.javacpp.geekmath;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.annotation.Namespace;
import org.bytedeco.javacpp.annotation.Platform;

@Platform(
    include = {"geekmath.hpp"},
    link = {"geekmathLib"})
@Namespace("geekmath")
public class Simple extends Pointer {
  static {
    Loader.load();
  }

  public Simple() {
    allocate();
  }

  public native void allocate();

  public native int add(int a, int b);
}
