# spark

## main usage

* Apache Spark™ is a multi-language engine for executing data engineering, data science, and machine learning on
  single-node machines or clusters.

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `spark`
* install `spark-tool`
* test spark with `spark-tool`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="spark.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_spark_3.2.1-debian-10-r78.dim"
      ```
4. install `spark`
    * prepare [spark.values.yaml](resources/spark/spark.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/spark:3.2.1-debian-10-r78"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-spark \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/spark-5.9.11.tgz \
              --values spark.values.yaml \
              --atomic
          ```
5. install `spark-tool`
    * prepare [spark.tool.yaml](resources/spark/spark.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f spark.tool.yaml
      ```

## test spark with spark-tool

1. connect to database
    * ```shell
      kubectl -n application \
          exec -it my-spark-worker-0 -- \
              spark-submit --master spark://my-spark-master-svc:7077 \
                  --class org.apache.spark.examples.SparkPi \
                  /opt/bitnami/spark/examples/jars/spark-examples_2.12-3.2.1.jar 5
      ```

## uninstallation

1. uninstall `spark-tool`
    * ```shell
      kubectl -n application delete -f spark.tool.yaml
      ```
2. uninstall `spark`
    * ```shell
      helm -n application uninstall my-spark \
          && kubectl -n application delete pvc data-my-spark-mariadb-0
      ```


