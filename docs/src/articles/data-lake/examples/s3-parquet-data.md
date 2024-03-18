# s3 parquet data

# prepare
1. prepare clickhouse service
    * [clickhouse with kubernetes](../../kubernetes/argocd/database/clickhouse/README.md)
    * [clickhouse with container](../../docker/software/database/clickhouse.md)
        + this article will use this as example
2. prepare s3 service
    * [minio with kubernetes](../../kubernetes/argocd/storage/minio/README.md)
    * [minio with container](../../docker/software/storage/minio.md)
        + this article will use this as example
3. download TLC trip record data from https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
    * for example: `fhvhv_tripdata_2023-11.parquet`

## interact with clickhouse
### by s3 function
1. upload data to s3
    * ```shell
      podman run --rm \
          -v $(pwd)/fhvhv_tripdata_2023-11.parquet:/data/fhvhv_tripdata_2023-11.parquet \
          --entrypoint bash \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://host.docker.internal:9000 minioadmin minioadmin \
              && mc mb --ignore-existing minio/tlc \
              && mc cp /data/fhvhv_tripdata_2023-11.parquet minio/tlc/data/fhvhv_tripdata_2023-11.parquet \
              && mc ls minio/tlc/data"
      ```
2. describe table
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "DESCRIBE s3('http://host.docker.internal:9000/tlc/data/fhvhv_tripdata_2023-11.parquet', 'minioadmin', 'minioadmin', 'Parquet')"
      ```
3. query data
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "SELECT * FROM s3('http://host.docker.internal:9000/tlc/data/fhvhv_tripdata_2023-11.parquet', 'minioadmin', 'minioadmin', 'Parquet') LIMIT 10"
      ```

### by s3 engine
1. create table with s3 engine
    * ```shell
      podman run --rm \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "CREATE TABLE IF NOT EXISTS fhvhv_trip(
                    \"hvfhs_license_num\" String COMMENT '',
                    \"dispatching_base_num\" String COMMENT '',
                    \"originating_base_num\" String COMMENT '',
                    \"request_datetime\" DateTime64 COMMENT '',
                    \"on_scene_datetime\" DateTime64 COMMENT '',
                    \"pickup_datetime\" DateTime64 COMMENT '',
                    \"dropoff_datetime\" DateTime64 COMMENT '',
                    \"PULocationID\" Int32 COMMENT '',
                    \"DOLocationID\" Int32 COMMENT '',
                    \"trip_miles\" Float64 COMMENT '',
                    \"trip_time\" Int64 COMMENT '',
                    \"base_passenger_fare\" Float64 COMMENT '',
                    \"tolls\" Float64 COMMENT '',
                    \"bcf\" Float64 COMMENT '',
                    \"sales_tax\" Float64 COMMENT '',
                    \"congestion_surcharge\" Float64 COMMENT '',
                    \"airport_fee\" Float64 COMMENT '',
                    \"tips\" Float64 COMMENT '',
                    \"driver_pay\" Float64 COMMENT '',
                    \"shared_request_flag\" String COMMENT '',
                    \"shared_match_flag\" String COMMENT '',
                    \"access_a_ride_flag\" String COMMENT '',
                    \"wav_request_flag\" String COMMENT '',
                    \"wav_match_flag\" String COMMENT ''
                ) ENGINE = S3('http://host.docker.internal:9000/tlc/data/fhvhv_tripdata_2023-11.parquet', 'minioadmin', 'minioadmin', 'Parquet')
                ORDER BY \"pickup_datetime\"
                COMMENT 'High Volume For-Hire Vehicle Trip Records'"
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
              --query "SELECT * FROM fhvhv_trip LIMIT 10"
      ```
