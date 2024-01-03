# cassandra

## main usage

* Apache Cassandra is an open source NoSQL distributed database trusted by thousands of companies for scalability and
  high availability without compromising performance. Linear scalability and proven fault-tolerance on commodity
  hardware or cloud infrastructure make it the perfect platform for mission-critical data.

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `cassandra`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="cassandra.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_cassandra_3.11.11-debian-10-r4.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_cassandra-exporter_2.3.4-debian-10-r478.dim"
      ```
3. install `cassandra`
    * prepare [cassandra.values.yaml](resources/cassandra/cassandra.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/cassandra:3.11.11-debian-10-r4" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r153" \
              "docker.io/bitnami/cassandra-exporter:2.3.4-debian-10-r478"
          ```
    * install with helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-cassandra \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/cassandra-7.7.3.tgz \
              --values cassandra.values.yaml \
              --atomic
          ```
4. install `cassandra-tool`
    * prepare [cassandra.tool.yaml](resources/cassandra/cassandra.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f cassandra.tool.yaml
      ```

## test with cassandra-tool

1. connect to `cassandra`
    * ```shell
      cat > test.cql <<EOF
      SHOW HOST;
      DESCRIBE CLUSTER;
      DESCRIBE KEYSPACES;
      CREATE KEYSPACE test_keyspace WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 3};
      DESCRIBE KEYSPACES;
      DESCRIBE TABLES;
      CREATE TABLE test_keyspace.emp(emp_id int PRIMARY KEY, emp_name text, emp_city text, emp_sal varint, emp_phone varint);
      DESCRIBE TABLES;
      SELECT * FROM test_keyspace.emp;
      INSERT INTO test_keyspace.emp (emp_id, emp_name, emp_city, emp_phone, emp_sal) VALUES(1,'ram', 'Hyderabad', 9848022338, 50000);
      INSERT INTO test_keyspace.emp (emp_id, emp_name, emp_city, emp_phone, emp_sal) VALUES(2,'robin', 'Hyderabad', 9848022339, 40000);
      INSERT INTO test_keyspace.emp (emp_id, emp_name, emp_city, emp_phone, emp_sal) VALUES(3,'rahman', 'Chennai', 9848022330, 45000);
      SELECT * FROM test_keyspace.emp;
      UPDATE test_keyspace.emp SET emp_city='Delhi',emp_sal=50000 WHERE emp_id=2;
      SELECT * FROM test_keyspace.emp;
      DELETE emp_sal FROM test_keyspace.emp WHERE emp_id=3;
      SELECT * FROM test_keyspace.emp;
      DELETE FROM test_keyspace.emp WHERE emp_id=3;
      SELECT * FROM test_keyspace.emp;
      DROP TABLE test_keyspace.emp;
      DESCRIBE TABLES;
      DROP KEYSPACE test_keyspace;
      DESCRIBE KEYSPACES;
      EOF
      POD_NAME=$(kubectl get pod -n application \
          -l "app.kubernetes.io/name=cassandra-tool" \
          -o jsonpath="{.items[0].metadata.name}") \
          && kubectl -n application cp test.cql $POD_NAME:/tmp/test.cql \
          && kubectl -n application exec -it deployment/cassandra-tool -- \
              bash -c 'cqlsh -u cassandra -p $CASSANDRA_PASSWORD my-cassandra.application 9042 -f /tmp/test.cql'
      ```

## uninstallation

1. uninstall `cassandra-tool`
    * ```shell
      kubectl -n application delete -f cassandra.tool.yaml
      ```
2. uninstall `cassandra`
    * ```shell
      helm -n application uninstall my-cassandra \
          && kubectl -n application delete pvc data-my-cassandra-0
      ```
