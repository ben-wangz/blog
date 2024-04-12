# kafka

## server

* ```shell
  mkdir -p kafka/data
  podman run --rm \
      --name kafka-server \
      --hostname kafka-server \
      -p 9092:9092 \
      -p 9094:9094 \
      -v $(pwd)/kafka/data:/bitnami/kafka/data \
      -e KAFKA_CFG_NODE_ID=0 \
      -e KAFKA_CFG_PROCESS_ROLES=controller,broker \
      -e KAFKA_CFG_CONTROLLER_QUORUM_VOTERS=0@kafka-server:9093 \
      -e KAFKA_CFG_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093,EXTERNAL://:9094 \
      -e KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,EXTERNAL://host.containers.internal:9094 \
      -e KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,EXTERNAL:PLAINTEXT,PLAINTEXT:PLAINTEXT \
      -e KAFKA_CFG_CONTROLLER_LISTENER_NAMES=CONTROLLER \
      -d docker.io/bitnami/kafka:3.6.2
  ```

## client

* list topics
    + ```shell
      BOOTSTRAP_SERVER=host.containers.internal:9094
      TOPIC=test-topic
      podman run --rm \
          -it docker.io/bitnami/kafka:3.6.2 kafka-topics.sh \
              --bootstrap-server $BOOTSTRAP_SERVER --list
      ```
* create topic
    + ```shell
      BOOTSTRAP_SERVER=host.containers.internal:9094
      TOPIC=test-topic
      podman run --rm \
          -it docker.io/bitnami/kafka:3.6.2 kafka-topics.sh \
              --bootstrap-server $BOOTSTRAP_SERVER \
              --create \
              --if-not-exists \
              --topic $TOPIC
      ```
* describe topic
    + ```shell
      BOOTSTRAP_SERVER=host.containers.internal:9094
      TOPIC=test-topic
      podman run --rm \
          -it docker.io/bitnami/kafka:3.6.2 kafka-topics.sh \
              --bootstrap-server $BOOTSTRAP_SERVER \
              --describe \
              --topic $TOPIC
      ```
* produce message
    + ```shell
      podman run --rm \
          -e BOOTSTRAP_SERVER=host.containers.internal:9094 \
          -e TOPIC=test-topic \
          -it docker.io/bitnami/kafka:3.6.2 bash \
              -c 'for message in $(seq 0 10); do echo $message |kafka-console-producer.sh --bootstrap-server $BOOTSTRAP_SERVER --topic $TOPIC; done'
      ```
* consume message
    + ```shell
      podman run --rm \
          -e BOOTSTRAP_SERVER=host.containers.internal:9094 \
          -e TOPIC=test-topic \
          -it docker.io/bitnami/kafka:3.6.2 kafka-console-consumer.sh \
              --bootstrap-server $BOOTSTRAP_SERVER \
              --topic $TOPIC \
              --from-beginning
      ```
