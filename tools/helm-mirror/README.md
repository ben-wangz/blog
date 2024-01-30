# helm-mirror

## what
* mirror helm charts from remote

## usage

* prepare container image
    + with pre-build image
        * ```shell
          podman pull ghcr.io/ben-wangz/blog-helm-mirror:main
          ```
    + build image from source
        * ```shell
          bash build.sh
          ```
* prepare [charts.yaml](charts.yaml)
* initialize charts
    + ```shell
      IMAGE=ghcr.io/ben-wangz/blog-helm-mirror:main
      #IMAGE=localhost/helm-mirror:1.0.0
      mkdir -p charts
      podman run --rm \
          -v $(pwd)/charts.yaml:/tmp/charts.yaml:ro \
          -v $(pwd)/charts:/data \
          --env CHART_YAML_FILE=/tmp/charts.yaml \
          --env DESTINATION=/data -it $IMAGE /app/mirror.sh
      ```
