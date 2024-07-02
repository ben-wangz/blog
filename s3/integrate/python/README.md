# s3-integrate-python

## key code snippets
1. entrypoint: [app.py](app.py)

## how to run
1. prepare minio service
2. build container image
    * ```shell
      bash s3/integrate/python/container/build.sh
      ```
3. run container
    * ```shell
      podman run --rm -it localhost/s3-integrate-python:latest
      ```