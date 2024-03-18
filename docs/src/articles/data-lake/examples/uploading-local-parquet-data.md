# uploading local parquet data

## prepare

1. prepare clickhouse service
    * [clickhouse with kubernetes](../../kubernetes/argocd/database/clickhouse/README.md)
    * [clickhouse with container](../../docker/software/database/clickhouse.md)
        + this article will use this as example
2. download TLC trip record data from https://www.nyc.gov/site/tlc/about/tlc-trip-record-data.page
    * for example: [yellow_tripdata_2023-11.parquet](https://d37ci6vzurychx.cloudfront.net/trip-data/yellow_tripdata_2023-11.parquet)

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
              --query "CREATE TABLE IF NOT EXISTS yellow_trip(
                  \"VendorID\" Int32 COMMENT 'A code indicating the TPEP provider that provided the record. 1= Creative Mobile Technologies, LLC; 2= VeriFone Inc.',
                  \"tpep_pickup_datetime\" DateTime64 COMMENT 'The date and time when the meter was engaged.',
                  \"tpep_dropoff_datetime\" DateTime64 COMMENT 'The date and time when the meter was disengaged.',
                  \"passenger_count\" Int32 COMMENT 'The number of passengers in the vehicle. This is a driver-entered value.',
                  \"trip_distance\" Float64 COMMENT 'The elapsed trip distance in miles reported by the taximeter.',
                  \"RatecodeID\" Int32 COMMENT 'The final rate code in effect at the end of the trip. 1= Standard rate; 2=JFK; 3=Newark; 4=Nassau or Westchester; 5=Negotiated fare; 6=Group ride',
                  \"store_and_fwd_flag\" String COMMENT 'This flag indicates whether the trip record was held in vehicle memory before sending to the vendor, aka \"store and forward,\" because the vehicle did not have a connection to the server. Y= store and forward trip; N= not a store and forward trip',
                  \"PULocationID\" Int32 COMMENT 'TLC Taxi Zone in which the taximeter was engaged',
                  \"DOLocationID\" Int32 COMMENT 'TLC Taxi Zone in which the taximeter was disengaged',
                  \"payment_type\" Int32 COMMENT 'A numeric code signifying how the passenger paid for the trip. 1= Credit card; 2= Cash; 3= No charge; 4= Dispute; 5= Unknown; 6= Voided trip',
                  \"fare_amount\" Float64 COMMENT 'The time-and-distance fare calculated by the meter.',
                  \"extra\" Float64 COMMENT 'Miscellaneous extras and surcharges. Currently, this only includes the \$0.50 and \$1 rush hour and overnight charges.',
                  \"mta_tax\" Float64 COMMENT '\$0.50 MTA tax that is automatically triggered based on the metered rate in use.',
                  \"tip_amount\" Float64 COMMENT 'Tip amount – This field is automatically populated for credit card tips. Cash tips are not included.',
                  \"tolls_amount\" Float64 COMMENT 'Total amount of all tolls paid in trip.',
                  \"improvement_surcharge\" Float64 COMMENT '\$0.30 improvement surcharge assessed trips at the flag drop. The improvement surcharge began being levied in 2015.',
                  \"total_amount\" Float64 COMMENT 'The total amount charged to passengers. Does not include cash tips.',
                  \"congestion_surcharge\" Float64 COMMENT 'Total amount collected in trip for NYS congestion surcharge.',
                  \"Airport_fee\" Float64 COMMENT '\$1.25 for pick up only at LaGuardia and John F. Kennedy Airports'
              ) ENGINE = MergeTree
              ORDER BY \"tpep_pickup_datetime\"
              COMMENT 'tlc yellow trip record data'"
      ```
2. uploading data to clickhouse
    * ```shell
      podman run --rm \
          -v $(pwd)/yellow_tripdata_2023-11.parquet:/data/yellow_tripdata_2023-11.parquet \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "INSERT INTO yellow_trip FROM INFILE '/data/yellow_tripdata_2023-11.parquet' FORMAT Parquet"
      ```
3. query data from clickhouse
    * ```shell
      podman run --rm \
          -v $(pwd)/yellow_tripdata_2023-11.parquet:/data/yellow_tripdata_2023-11.parquet \
          --entrypoint clickhouse-client \
          -it docker.io/clickhouse/clickhouse-server:23.11.5.29-alpine \
              --host host.containers.internal \
              --port 19000 \
              --user ben \
              --password 123456 \
              --query "SELECT * FROM yellow_trip LIMIT 10"
      ```
