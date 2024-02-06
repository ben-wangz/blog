# minio

## server

```shell
mkdir -p $(pwd)/minio/data
podman run --rm \
    --name minio-server \
    -p 9000:9000 \
    -p 9001:9001 \
    -v $(pwd)/minio/data:/data \
    -d docker.io/minio/minio:latest server /data --console-address :9001
```

## web console

* address: http://localhost:9001
* access key: minioadmin
* access secret: minioadmin

## client

* address: http://localhost:9000
* ```shell
  podman run --rm \
      --entrypoint bash \
      -it docker.io/minio/mc:latest \
      -c "mc alias set minio http://host.docker.internal:9000 minioadmin minioadmin \
          && mc ls minio \
          && mc mb --ignore-existing minio/test \
          && mc cp /etc/hosts minio/test/etc/hosts \
          && mc ls --recursive minio"
  ```