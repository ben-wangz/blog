# binary mirror

## what
* mirror container images from remote to private repository

## usage
* ```shell
  export REGISTRY_USERNAME=xxx \
      && export REGISTRY_PASSWORD='yyy' \
      && export TARGET_REGISTRY=registry.cn-hangzhou.aliyuncs.com \
      && export REPOSITORY_PREFIX=mirror-pub/ \
      && export KEEP_STRUCTURE=false \
      && bash mirror.sh
  ```