# hello-world for apache arrow

## pre install
Apache Arrow 15.0.2   https://arrow.apache.org/install/

## how to run
```shell
./gradlew :arrow:c-data-interface:app:shadowJar
./gradlew :arrow:c-data-interface:bridge:customJar
java -cp "$(pwd)/arrow/c-data-interface/app/build/libs/app-1.0.0-all.jar:$(pwd)/arrow/c-data-interface/bridge/build/libs/bridge-1.0.0.jar" tech.geekcity.arrow.c.data.app.Application
```
