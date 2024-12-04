# container registry with s3 storage(minio)

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
4. add hosts to resolve `host.containers.internal`
    * ```shell
      echo "127.0.0.1 host.containers.internal" >> /etc/hosts
      ```
5. [testing](test.md)
