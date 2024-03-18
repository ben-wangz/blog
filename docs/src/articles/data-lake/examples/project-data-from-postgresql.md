# project data from postgresql

## prepare

1. prepare clickhouse service
    * [clickhouse with kubernetes](../../kubernetes/argocd/database/clickhouse/README.md)
    * [clickhouse with container](../../docker/software/database/clickhouse.md)
        + this article will use this as example
2. prepare postgres service
    * [with kubernetes](../../kubernetes/argocd/database/postgresql/README.md)
    * [with container](../../docker/software/database/postgresql.md)
        + this article will use this as example
3. download data from https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
    * [taxi+zone_lookup.csv](https://s3.amazonaws.com/nyc-tlc/misc/taxi+_zone_lookup.csv)
    * ```shell
      curl -LO https://s3.amazonaws.com/nyc-tlc/misc/taxi+_zone_lookup.csv
      ```
4. prepare table and data in postgresql
    * create database `tlc`
        + ```shell
          podman run --rm \
              --env PGPASSWORD=postgresql \
              --entrypoint psql \
              -it docker.io/library/postgres:15.2-alpine3.17 \
                  --host host.containers.internal \
                  --port 5432 \
                  --username postgres \
                  --dbname postgres \
                  --command "CREATE DATABASE tlc"
          ```
    * create table `taxi_zone_lookup`
        + ```shell
          podman run --rm \
              --env PGPASSWORD=postgresql \
              --entrypoint psql \
              -it docker.io/library/postgres:15.2-alpine3.17 \
                  --host host.containers.internal \
                  --port 5432 \
                  --username postgres \
                  --dbname tlc \
                  --command "CREATE TABLE IF NOT EXISTS taxi_zone_lookup (
                      \"LocationID\" INTEGER,
                      \"Borough\" VARCHAR(255),
                      \"Zone\" VARCHAR(255),
                      \"service_zone\" VARCHAR(255),
                      PRIMARY KEY (\"LocationID\")
                  )"
          ```
    * upload csv data to table `taxi_zone_lookup`
        + ```shell
          podman run --rm \
              -v $(pwd)/taxi+_zone_lookup.csv:/data/taxi+_zone_lookup.csv \
              --env PGPASSWORD=postgresql \
              --entrypoint psql \
              -it docker.io/library/postgres:15.2-alpine3.17 \
                  --host host.containers.internal \
                  --port 5432 \
                  --username postgres \
                  --dbname tlc \
                  --command "\\COPY taxi_zone_lookup FROM '/data/taxi+_zone_lookup.csv' WITH CSV HEADER DELIMITER ','"
          ```
    * query data from `taxi_zone_lookup`
        + ```shell
          podman run --rm \
              -v $(pwd)/taxi+_zone_lookup.csv:/data/taxi+_zone_lookup.csv \
              --env PGPASSWORD=postgresql \
              --entrypoint psql \
              -it docker.io/library/postgres:15.2-alpine3.17 \
                  --host host.containers.internal \
                  --port 5432 \
                  --username postgres \
                  --dbname tlc \
                  --command "SELECT * FROM taxi_zone_lookup LIMIT 10"
          ```

## interact with clickhouse
1. create table with PostgreSQL engine
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "CREATE DATABASE IF NOT EXISTS postgresql_tlc
                ENGINE = PostgreSQL('host.containers.internal:5432', 'postgres', 'postgres', 'postgresql', 'public', 0)
                COMMENT 'project data from database named tlc in postgresql'"
      ```
2. query data
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "SELECT * FROM postgresql_tlc.taxi_zone_lookup LIMIT 10"
      ```