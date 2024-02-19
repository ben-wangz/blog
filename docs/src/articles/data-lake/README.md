---
title: data-lake
icon: folder
category:
  - data-lake
tag:
  - big-data
  - data-lake
index: false
---

## mindmap

```mermaid
mindmap
  root((data-lake))
    sub(concepts)
      sub(storage)
      sub(processing)
      sub(metadata)
      sub(management)
      sub(beyond technology)
    sub(implementation pieces)
      sub(storage layer)
      sub(processing layer)
      sub(metadata layer)
      sub(workflow and scheduling)
      sub(api and sdk)
      sub(machine learning and ai)
    sub(use cases)
```

## what's a data lake
* A data lake is a centralized repository that allows you to store all your structured and unstructured data at any scale
* It's a concept, similar to cloud computing, but not a specifc technology
* It's an architectural approach that allows enterprises consolidate large heterogeneous data assets at scale and uncover actionable insights from the consolidated data through various types of analytics

## concepts
1. storage
    * storage is a fundamental component of any data lake
    * data lakes should be able to store structured, semi-structured, and unstructured data
    * in usual, data lakes are built on scalable and elastic storage
    * it's very important to integrate with other storage systems for a data lake to accessing and analyzing data from different sources
2. processing
    * data lakes should be able to process data at big scale
    * data lakes may support both batch processing and stream processing
    * data lakes should support different processing methods, such as SQL, piece of code, etc.
3. metadata
    * it's a key fundamental component of a data lake which keeps track of all the data assets
    * metadata is used to discover, understand, and govern data
    * features for metadata management including
        + auto-discovery
        + auto-classification
        + auto-tagging
        + data lineage
        + user customization
4. management
    * data governance
    * data lifecycle management
    * data security
    * data privacy
5. beyond technology

## implementation pieces
1. storage layer
    * use `s3-compatible storage`, such as minio, to store files
        + minio: [docker](../docker/software/storage/minio.md) | [k8s](../kubernetes/argocd/storage/minio/README.md)
    * use `clickhouse` to store tables
        + [docker](../docker/software/database/clickhouse.md) | [k8s](../kubernetes/argocd/database/clickhouse/README.md)
        + why clickhouse?
            1. it's a column-oriented database management system
            2. merge tree engine is powerful to store and query time series data
            3. support a lot of different engines to connect to other storage systems, such as s3, kafka, postgresql, etc.
2. processing layer
    * flink
        + basic tutorials
        + [flink on k8s](../kubernetes/argocd/flink/README.md)
        + examples of core features for data lake
            1. [sink to s3 with parquet format](https://github.com/ben-wangz/blog/blob/main/flink/connectors/s3/src/main/java/tech/geekcity/flink/connectors/s3/SinkToS3WithParquet.java)
            2. [source from s3 with parquet format](https://github.com/ben-wangz/blog/blob/main/flink/connectors/s3/src/main/java/tech/geekcity/flink/connectors/s3/SourceFromS3WithParquet.java)
            3. [sink to jdbc]()
            4. [source from jdbc]()
            5. both clickhouse, cockroach, cassandra support jdbc
3. metadata layer
    * datahub
4. workflow and scheduling
    * argo workflow
5. api and sdk
    * self developed
6. machine learning and ai

<AutoCatalog />