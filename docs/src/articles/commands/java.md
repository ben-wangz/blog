# java

## show gc info

```shell
jstat -gcutil $pid 1000 100
```

## gradle wrapper

```shell
podman run --rm -v $(pwd):/app -w /app docker.io/library/gradle:8.7.0-jdk11 gradle :wrapper
```