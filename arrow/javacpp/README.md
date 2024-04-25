# hello-world for apache arrow

## how to run
```shell
./gradlew :arrow:javacpp:app:shadowJar
./gradlew :arrow:javacpp:geekmath:jar
./gradlew :arrow:javacpp:geekmath:javacppJar
CURRENT_PATH=$(pwd)
java -cp "$CURRENT_PATH/arrow/javacpp/app/build/libs/app-0.1.0-all.jar:$CURRENT_PATH/garrow/javacpp/geekmath/build/libs/geekmath-0.1.0.jar:$CURRENT_PATH/arrow/javacpp/geekmath/build/libs/geekmath-0.1.0-linux-x86_64.jar" tech.geekcity.javacpp.app.Application
```
