# uploading local csv data

## prepare 
1. prepare clickhouse service
    * [clickhouse with kubernetes](../../kubernetes/argocd/database/clickhouse/README.md)
    * [clickhouse with container](../../docker/software/database/clickhouse.md)
        + this article will use this as example
2. download taxi zone lookup table for TLC trip record data from https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
    * for example: `https://s3.amazonaws.com/nyc-tlc/misc/taxi+_zone_lookup.csv`

## interact with clickhouse
1. create table
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
          --host host.containers.internal \
          --port 19000 \
          --user ben \
          --password 123456 \
          --query "CREATE TABLE IF NOT EXISTS taxi_zone_lookup(
            \"LocationID\" Int32 COMMENT 'id of the location',
            \"Borough\" String COMMENT '',
            \"Zone\" String COMMENT '',
            \"service_zone\" String COMMENT '',
            PRIMARY KEY (\"LocationID\")
          ) ENGINE = MergeTree
            COMMENT 'taxi zone lookup table for tlc trip record data'"
      ```
2. uploading data to clickhouse
    * ```shell
      podman run --rm \
          -v $(pwd)/taxi+_zone_lookup.csv:/data/taxi+_zone_lookup.csv \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "INSERT INTO taxi_zone_lookup FROM INFILE '/data/taxi+_zone_lookup.csv' FORMAT CSVWithNames"
      ```
3. query data from clickhouse
    * ```shell
      podman run --rm \
          -v $(pwd)/taxi+_zone_lookup.csv:/data/taxi+_zone_lookup.csv \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "SELECT * FROM taxi_zone_lookup LIMIT 10"
      ```