---
title: intro
icon: circle-info
cover: /assets/images/black-hole-hero-f872641.jpg
---

## Wang Zhi 

* email: ben.wangz@foxmail.com

## Summary

I am an experienced coder with a focus on Java development. Over the past 8 years, I have developed many data processing platforms at various companies, including Alibaba and ZhejiangLab and tianrang-inc. My expertise lies in designing and implementing scalable and efficient systems that can handle very large volumes of data.

## Technical Skills

* Java is my primary language for coding.
* familiar with Kubernetes, especially for storages, ci/cd workflows, flink on k8s and so on. please check my blog for details.
* familiar with stream processing, especially for Apache Flink

## Experience

### Senior Engineer in ZhejiangLab
* time: 2023 - now
* leading a team of 5 people
* belongs to astronomy center
    + designing and developing a data platform
        + to support CSST(China Space Station Telescope)
        + to support Cosmic Antenna
        + related techniques: check datalake in my blog for details

### Senior Engineer in ZhejiangLab
* time: 2021 - 2023
* belongs to big data center
* leading a team of 5-10 people
* main job 1: maintaining old data processing platform and designing a new one with pluggable architecture
    + data formats
        * old platform: csv uploaded and tables from database
        * new platform: driven by calcite framework which support csv from s3, tables from common relational databases, graphs from neo4j/jena/rdf(TURTLE), etc.
    + data management
        * old platform: data is managed by postgresql/greenplum
        * new platform: built-in s3(s3) and remote s3, relational databases
    + algorithms
        * old platform: only built-in algorithms
        * new platform: pluggable and custom algorithms, which can be written in java, python, etc.
    + next features for new platform
        * executing engines to support flink jobs which can be compatible with current calcite framework
        * applications can be pluggable
* main job 3: maintaining old k8s cluster and designing a new one which is ready for production
    + all softwares managed by helm charts
    + less than one person per month for maintaining three clusters, which contains more than 300 services

### Professional Data Processing Engineer in 天壤智能
* time: 2019 - 2021
* leading a team of 3-5 people
* main job
    + designed and implemented a data processing platform named pandora, which based on flink, for advertisement putting data analysis
        * table and sql api of flink to refactor business logic into small pieces for better maintainability
        * flink on k8s
        * oss and kafka as the storage to handle both batch and stream data
    + refactor the pipelines and the code for feature extracting pipelines and predicting algorithms for a recommendation system at China Merchants Bank
        * optimization for spark jobs
        * designing interfaces for algorithms

### Senior Java Developer in Alibaba Group
* time: 2015 - 2019
* belongs to search dump team at Search Department
* main job: data processing platform for search engine
* owned business
    + designed and implemented an image data processing platform for image searching in Taobao: pailitao is the main user of this platform
        * standards for c++ image processing modules, which based on protobuf, JNI, cmake, etc.
        * make standalone image processing modules to be a distributed system
        * image feature extraction, image similarity calculation, clustering, etc.
        * loading data into search engine
        * no online fault for more than 3 years after the platform was put into use
    + maintained a data processing platform for Taobao/Tmall main search
        * online fault not larger than P3
        * only two people, including me, handled the whole taobao/tmall data for searching for nearly 2 years
    + maintained data processing platforms for 1688, aliexpress, etc.
        * no online fault
* kpi: 3.75 all the time except 3.5+(once)

### intern at Alibaba Group
* time: 2014.07 - 2015.04
* belongs to search dump team at Search Department
* main job: data processing platform for search engine
* owned business
    + Independently developed a distributed data synchronization tool, from MySQL to HBase, which was secure, high performance and widely used for more than 4 years

## Education

### Bachelor's degree of Software Engineering
** NorthEastern University ** (2009 - 2013)

### Master's degree of Software Engineering
** NorthEastern University ** (2013 - 2015)

## papers published

* Song, Jie, HongYan He, Zhi Wang, Ge Yu, and Jean-Marc Pierson. "Modulo based data placement algorithm for energy consumption optimization of MapReduce system." Journal of Grid Computing 16 (2018): 409-424.
* 宋杰, 王智, 李甜甜, and 于戈. "一种优化 MapReduce 系统能耗的数据布局算法." 软件学报 26, no. 8 (2015): 2091-2110.
* Song, Jie, Tiantian Li, Zhi Wang, and Zhiliang Zhu. "Study on energy-consumption regularities of cloud computing systems by a novel evaluation model." Computing 95 (2013): 269-287.
* Song, Jie, Chaopeng Guo, Zhi Wang, Yichan Zhang, Ge Yu, and Jean-Marc Pierson. "HaoLap: A Hadoop based OLAP system for big data." Journal of Systems and Software 102 (2015): 167-181.
* 郭朝鹏, 王智, 韩峰, 张一川, and 宋杰. "HaoLap: 基于 Hadoop 的海量数据 OLAP 系统." 计算机研究与发展 S1 (2013): 378-383.
* 宋杰, 侯泓颖, 王智, and 朱志良. "云计算环境下改进的能效度量模型." 浙江大学学报: 工学版 1 (2013): 44-52.

## projects outside class

## Certifications

* English: Certificate VI
* [Red Hat Certified Engineer](RHCE_Zhi_Wang.pdf.md)
    + early in my high school: 2010.08.31