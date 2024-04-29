# hello-world for apache arrow

## pre install
Apache Arrow 15.0.2   https://arrow.apache.org/install/

## how to run
```shell
./gradlew :arrow:CDataCppBridge:app:shadowJar
./gradlew :arrow:CDataCppBridge:CDataCppBridge:customJar
CURRENT_PATH=$(pwd)
java -cp "$CURRENT_PATH/arrow/CDataCppBridge/app/build/libs/app-0.1.0-all.jar:$CURRENT_PATH/arrow/CDataCppBridge/CDataCppBridge/build/libs/CDataCppBridge-0.1.0.jar" tech.geekcity.javacpp.app.Application
