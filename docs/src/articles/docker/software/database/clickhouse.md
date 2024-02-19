# clickhouse

## server

```shell
mkdir -p clickhouse/{data,logs}
podman run --rm \
    --ulimit nofile=262144:262144 \
    --name clickhouse-server \
    -p 18123:8123 \
    -p 19000:9000 \
    -p 19005:9005 \
    -p 19004:9004 \
    -v $(pwd)/clickhouse/data:/var/lib/clickhouse \
    -v $(pwd)/clickhouse/logs:/var/log/clickhouse-server \
    -e CLICKHOUSE_DB=my_database \
    -e CLICKHOUSE_DEFAULT_ACCESS_MANAGEMENT=1 \
    -e CLICKHOUSE_USER=ben \
    -e CLICKHOUSE_PASSWORD=123456 \
    -d docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine
```

## web console

* address: http://localhost:18123/
* ```shell
  echo 'SELECT version()' | curl 'http://ben:123456@localhost:18123/' --data-binary @-
  ```
* ```shell
  echo 'SELECT version()' | curl 'http://localhost:18123/?user=ben&password=123456' --data-binary @-
  ```

## native client

* address: http://localhost:19000
* ```shell
  podman run --rm \
      --entrypoint clickhouse-client \
      -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
      --host host.containers.internal \
      --port 19000 \
      --user ben \
      --ask-password
  ``` 
* ```shell
  podman run --rm \
      --entrypoint clickhouse-client \
      -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
      --host host.containers.internal \
      --port 19000 \
      --user ben \
      --password 123456
  ``` 
* ```shell
  podman run --rm \
      --entrypoint clickhouse-client \
      -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
      --host host.containers.internal \
      --port 19000 \
      --user ben \
      --password 123456 \
      --query "select version()"
  ``` 

## visual client

* ```shell
  podman run --rm -p 8080:80 -d docker.io/spoonest/clickhouse-tabix-web-client:stable
  ```
* http://localhost:8080/
* ref: https://github.com/tabixio/tabix

## postgresql client

* ```shell
  podman run --rm \
      --env PGPASSWORD=123456 \
      --entrypoint psql \
      -it docker.io/library/postgres:15.2-alpine3.17 \
      --host host.containers.internal \
      --port 19005 \
      --username ben \
      --dbname default \
      --command 'select version()'
  ```

## mysql client

* ```shell
  podman run --rm \
      -e MYSQL_PWD=123456 \
      -it docker.io/library/mariadb:11.2.2-jammy \
      mariadb \
      --host host.containers.internal \
      --port 19004 \
      --user ben \
      --database default \
      --execute 'select version()'
  ```
