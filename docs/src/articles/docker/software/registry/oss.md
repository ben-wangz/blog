# container registry with s3 storage(oss from aliyun)

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
          -d docker.io/library/registry:2
      ```
3. [tests](test.md) not passed, the same problem as [this issue 4452](https://github.com/distribution/distribution/issues/4452)