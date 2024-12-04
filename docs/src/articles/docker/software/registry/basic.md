# basic container registry

## container registry with http

1. ```shell
   podman run --rm --name registry -p 5000:5000 -d docker.io/library/registry:2
   ```
2. [testing](#testing)


## with s3 storage(minio)

1. [minio server](../storage/minio.md)
2. create bucket named `blog`
    * ```shell
        podman run --rm \
            --entrypoint bash \
            -it docker.io/minio/mc:latest \
            -c "mc alias set minio http://host.containers.internal:9000 minioadmin minioadmin \
                && mc ls minio \
                && mc mb --ignore-existing minio/blog \
                && mc ls --recursive minio"
      ```
3. container registry with envs to override the config to use minio as s3 storage
    * ```shell
      podman run --rm \
          --name registry \
          -p 5000:5000 \
          -e REGISTRY_STORAGE=s3 \
          -e REGISTRY_STORAGE_S3_ACCESSKEY=minioadmin \
          -e REGISTRY_STORAGE_S3_SECRETKEY=minioadmin \
          -e REGISTRY_STORAGE_S3_REGION=us-east-1 \
          -e REGISTRY_STORAGE_S3_BUCKET=blog \
          -e REGISTRY_STORAGE_S3_ROOTDIRECTORY=components/container-registry \
          -e REGISTRY_STORAGE_S3_SECURE=false \
          -e REGISTRY_STORAGE_S3_REGIONENDPOINT=http://host.containers.internal:9000 \
          -e REGISTRY_STORAGE_S3_FORCEPATHSTYLE=true \
          -d docker.io/library/registry:2
      ```
4. [testing](#testing)

### with s3 storage(oss from aliyun)

1. assuming
    * we have a bucket named `blog` in oss
    * ```shell
      OSS_ACCESS_KEY_ID=your_access_key_id
      OSS_ACCESS_KEY_SECRET=your_access_key_secret
      OSS_REGION=oss-cn-hangzhou
      OSS_ENDPOINT=http://oss-cn-hangzhou-internal.aliyuncs.com
      ```
2. container registry with envs to override the config to use oss as s3 storage
    * ```shell
      podman run --rm \
          --name registry \
          -p 5000:5000 \
          -e REGISTRY_STORAGE=s3 \
          -e REGISTRY_STORAGE_S3_ACCESSKEY=$OSS_ACCESS_KEY_ID \
          -e REGISTRY_STORAGE_S3_SECRETKEY=$OSS_ACCESS_KEY_SECRET \
          -e REGISTRY_STORAGE_S3_REGION=$OSS_REGION \
          -e REGISTRY_STORAGE_S3_BUCKET=blog \
          -e REGISTRY_STORAGE_S3_ROOTDIRECTORY=components/container-registry \
          -e REGISTRY_STORAGE_S3_SECURE=true \
          -e REGISTRY_STORAGE_S3_REGIONENDPOINT=$OSS_ENDPOINT \
          -e REGISTRY_STORAGE_S3_FORCEPATHSTYLE=true \
          -d docker.io/library/registry:2.8.1
      ```
3. tests not passed, the same problem as [this issue 4452](https://github.com/distribution/distribution/issues/4452)

## testing

1. ```shell
   podman pull docker.io/library/alpine:latest
   podman tag docker.io/library/alpine:latest localhost:5000/alpine:latest
   podman push --tls-verify=false localhost:5000/alpine:latest
   ```
2. ```shell
   podman image rm docker.io/library/alpine:latest localhost:5000/alpine:latest
   podman pull --tls-verify=false localhost:5000/alpine:latest
   ```
