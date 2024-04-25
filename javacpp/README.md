# javacpp

## how to run
```shell
./gradlew :javacpp:app:shadowJar
./gradlew :javacpp:geekmath:jar
./gradlew :javacpp:geekmath:javacppJar
CURRENT_PATH=$(pwd)
java -cp "$CURRENT_PATH/javacpp/app/build/libs/app-0.1.0-all.jar:$CURRENT_PATH/javacpp/geekmath/build/libs/geekmath-0.1.0.jar:$CURRENT_PATH/javacpp/geekmath/build/libs/geekmath-0.1.0-linux-x86_64.jar" tech.geekcity.javacpp.app.Application
```
