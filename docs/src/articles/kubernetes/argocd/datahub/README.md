# datahub

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. elastic search is ready
4. kafka with zookeeper mode is ready
5. mariadb/mysql is ready

## installation

1. create secret `datahub-credentials`
    * ```shell
      kubectl -n application \
          create secret generic datahub-credentials \
          --from-literal=neo4j-password="$(kubectl get secret neo4j-credentials --namespace database -o jsonpath='{.data.NEO4J_AUTH}' | base64 -d | cut -d/ -f2)" \
          --from-literal=mysql-root-password="$(kubectl get secret mariadb-credentials --namespace database -o jsonpath='{.data.mariadb-root-password}' | base64 -d)" \
          --from-literal=security.protocol="SASL_PLAINTEXT" \
          --from-literal=sasl.mechanism="SCRAM-SHA-256" \
          --from-literal=sasl.jaas.config="org.apache.kafka.common.security.scram.ScramLoginModule required username=\"user1\" password=\"$(kubectl get secret kafka-user-passwords --namespace database -o jsonpath='{.data.client-passwords}' | base64 -d | cut -d , -f 1)\";"
      ```
2. prepare `datahub.yaml`
    * ```yaml
      <!-- @include: datahub.yaml -->
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f datahub.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/datahub
      ```