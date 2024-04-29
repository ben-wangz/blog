# hello-world for apache arrow

## how to run
```shell
./gradlew :arrow:javacpp:app:shadowJar
./gradlew :arrow:javacpp:geekmath:customJar
CURRENT_PATH=$(pwd)
java -cp "$CURRENT_PATH/arrow/javacpp/app/build/libs/app-0.1.0-all.jar:$CURRENT_PATH/arrow/javacpp/geekmath/build/libs/geekmath-0.1.0.jar" tech.geekcity.javacpp.app.Application
```