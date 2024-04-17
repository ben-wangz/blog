# kafka

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `kafka.yaml`
    * ::: code-tabs#shell
      @tab kafka-with-kraft-minimal
      ```yaml
      <!-- @include: kafka-minimal.yaml -->
      ```
      @tab kafka-with-zookeeper-minimal
      ```yaml
      <!-- @include: kafka-with-zookeeper-minimal.yaml -->
      ```
      @tab kafka-with-kraft
      ```yaml
      <!-- @include: kafka.yaml -->
      ```
      @tab kafka-with-zookeeper
      ```yaml
      <!-- @include: kafka-with-zookeeper.yaml -->
      ```
      :::
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f kafka.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/kafka
      ```

## setup kafka-client-tools

1. create `client-properties`
    * ```shell
      kubectl -n database \
          create secret generic client-properties \
          --from-literal=client.properties="$(printf "security.protocol=SASL_PLAINTEXT\nsasl.mechanism=SCRAM-SHA-256\nsasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$(kubectl get secret kafka-user-passwords --namespace database -o jsonpath='{.data.client-passwords}' | base64 -d | cut -d , -f 1)\";\n")"
      ```
2. prepare `kafka-client-tools.yaml`
    + ```yaml
      <!-- @include: kafka-client-tools.yaml -->
      ```
3. apply to k8s
    + ```shell
      kubectl -n database apply -f kafka-client-tools.yaml
      ```

## check with client

* list topics
    + ```shell
      kubectl -n database exec -it deployment/kafka-client-tools -- bash -c \
          'kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVER --command-config $CLIENT_CONFIG_FILE --list'
      ```
* create topic
    + ```shell
      kubectl -n database exec -it deployment/kafka-client-tools -- bash -c \
          'kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVER --command-config $CLIENT_CONFIG_FILE --create --if-not-exists --topic test-topic'
      ```
* describe topic
    + ```shell
      kubectl -n database exec -it deployment/kafka-client-tools -- bash -c \
          'kafka-topics.sh --bootstrap-server $BOOTSTRAP_SERVER --command-config $CLIENT_CONFIG_FILE --describe --topic test-topic'
      ```
* produce message
    + ```shell
      kubectl -n database exec -it deployment/kafka-client-tools -- bash -c \
          'for message in $(seq 0 10); do echo $message | kafka-console-producer.sh --bootstrap-server $BOOTSTRAP_SERVER --producer.config $CLIENT_CONFIG_FILE --topic test-topic; done'
      ```
* consume message
    + ```shell
      kubectl -n database exec -it deployment/kafka-client-tools -- bash -c \
          'kafka-console-consumer.sh --bootstrap-server $BOOTSTRAP_SERVER --consumer.config $CLIENT_CONFIG_FILE --topic test-topic --from-beginning'
      ```
