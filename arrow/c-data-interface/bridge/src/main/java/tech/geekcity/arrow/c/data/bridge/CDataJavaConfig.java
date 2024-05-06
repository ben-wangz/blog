package tech.geekcity.arrow.c.data.bridge;

import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
    target = "tech.geekcity.arrow.c.data.bridge.CDataJavaToCppExample",
    value =
        @Platform(
            include = {"CDataCppBridge.h"},
            link = {"CDataCppBridgeLib"}))
public class CDataJavaConfig implements InfoMapper {

  @Override
  public void map(InfoMap infoMap) {}
}
