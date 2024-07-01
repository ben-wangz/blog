# s3-integrate-python

## key code snippets
1. entrypoint: [app.py](app.py)
2. download file from http and write it into s3: [http_to_s3.py](http_to_s3.py)

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