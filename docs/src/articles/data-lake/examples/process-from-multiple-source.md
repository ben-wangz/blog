# process from multiple source

## prepare
1. [prepare MergeTree table named `yellow_trip`](uploading-local-parquet-data.md)
2. [prepare s3 table named `fhvhv_trip`](s3-parquet-data.md#by-s3-engine)
3. [prepare postgres table named `taxi_zone_lookup`](project-data-from-postgresql.md)
 
## interact with clickhouse
1. union `yellow_trip` and `fhvhv_trip`, and then join with `taxi_zone_lookup`
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "SELECT pickup_datetime, dropoff_datetime, PULocationID, borough, zone, service_zone 
                  FROM (
                      SELECT pickup_datetime, dropoff_datetime, PULocationID
                      FROM fhvhv_trip 
                      UNION ALL (
                          SELECT tpep_pickup_datetime as pickup_datetime, tpep_dropoff_datetime as dropoff_datetime, PULocationID FROM yellow_trip
                      )
                  ) join_table LEFT JOIN postgresql_tlc.taxi_zone_lookup AS zone_lookup ON join_table.PULocationID = zone_lookup.locationid
                  LIMIT 10"
      ```
