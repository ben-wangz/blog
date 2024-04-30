# datahub

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. elastic search is ready
4. kafka with zookeeper mode is ready
5. mariadb/mysql is ready

## installation

1. create secret `datahub-credentials`
    * ::: code-tabs#shell
      @tab kafka-sasl-plaintext
      ```shell
      kubectl -n application \
          create secret generic datahub-credentials \
          --from-literal=mysql-root-password="$(kubectl get secret mariadb-credentials --namespace database -o jsonpath='{.data.mariadb-root-password}' | base64 -d)" \
          --from-literal=security.protocol="SASL_PLAINTEXT" \
          --from-literal=sasl.mechanism="SCRAM-SHA-256" \
          --from-literal=sasl.jaas.config="org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$(kubectl get secret kafka-user-passwords --namespace database -o jsonpath='{.data.client-passwords}' | base64 -d | cut -d , -f 1)\";"
      ```
      @tab kafka-plaintext
      ```shell
      kubectl -n application \
          create secret generic datahub-credentials \
          --from-literal=mysql-root-password="$(kubectl get secret mariadb-credentials --namespace database -o jsonpath='{.data.mariadb-root-password}' | base64 -d)"
      ```
      :::
2. prepare `datahub.yaml`
    * ::: code-tabs#shell
      @tab kafka-sasl-plaintext
      ```yaml
      <!-- @include: datahub.yaml -->
      ```
      @tab kafka-plaintext
      ```yaml
      <!-- @include: datahub-kafka-plaintext.yaml -->
      ```
      :::
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f datahub.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/datahub
      ```

## visit with browser

1. extract credentials
    * ```shell
      kubectl -n application get secret datahub-user-secret -o jsonpath='{.data.user\.props}' | base64 -d
      ```
2. with http
    * datahub.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP datahub.dev.geekcity.tech` to `/etc/hosts`
    * datahub frontend: `https://datahub.dev.geekcity.tech:32443`
    * api: https://datahub.dev.geekcity.tech:32443/openapi/swagger-ui/index.html

## ingest metadata from s3
