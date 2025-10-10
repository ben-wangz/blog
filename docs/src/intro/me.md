---
title: intro
icon: circle-info
cover: /assets/images/black-hole-hero-f872641.jpg
---

## Wang Zhi 

* email: ben.wangz@foxmail.com
* blog: https://blog.geekcity.tech

## Summary

* experienced Java developer with 10+ years of expertise in designing and implementing scalable data processing platforms. 
* proficient in Apache Flink, Kubernetes, and handling large volumes of data. 
* proven ability to lead teams and deliver high-performance systems.
* achieved developing data processing platforms for various companies, including Alibaba Group, ZhejiangLab and tianrang-inc.

## Technical Skills

* Java is my primary programming language, but I'm comfortable working with other languages like Python and C/C++, and reading code in Go, Scala, and so on.
* familiar with Kubernetes, especially for storages, ci/cd workflows, flink on k8s and so on.
* familiar with stream processing, especially for Apache Flink.

## Experience

### astronomy center of ZhejiangLab, Senior Engineer(2023 - now)
* leading a team of 6 people
* designing and developing a data platform
    + related techniques: check [achievements](../intro/achievements/README.md) in my blog for details
* cooperated with multiple teams to design/implement data processing platforms or to deliver on-demand systems: refer to [projects](../intro/projects/README.md)
    + collaborating with scientists on the FAST project at the National Astronomical Observatories of China, Chinese Academy of Sciences
    + collaborating with scientists on the CSST project at the National Astronomical Observatories of China, Chinese Academy of Sciences
    + collaborating with scientists on the LHAASO project
    + collaborating with scientists on the cnSRC@SRCNet initiative of the SKA (Square Kilometre Array) project, where I was responsible for designing and building the k8s environment while investigating and implementing all recommended services from the SKAO (Square Kilometre Array Observatory)
        * https://www.shao.ac.cn/2020Ver/xwdt/kyjz/202504/t20250418_7604408.html
        * https://mp.weixin.qq.com/s/jPgXYWWPxY85gAUchvVktg

### big data center of ZhejiangLab, Senior Engineer(2021 - 2023)
* leading a team of 5-10 people
* maintaining old data processing platform and designing a new one with pluggable architecture
    + data formats
        * old platform: csv uploaded and tables from database
        * new platform: driven by calcite framework which support csv from s3, tables from common relational databases, graphs from neo4j/jena/rdf(TURTLE), etc.
    + data management
        * old platform: data is managed by postgresql/greenplum
        * new platform: built-in s3(s3) and remote s3, relational databases
    + algorithms
        * old platform: only built-in algorithms
        * new platform: pluggable and custom algorithms, which can be written in java, python, etc.
    + next(not finished) features for new platform
        * executing engines to support flink jobs which can be compatible with current calcite framework
        * pluggable applications 
    + open source codes
        * old platform: https://gitee.com/zhijiangtianshu/nebula
        * new platform: https://github.com/lab-zj/data-hub
* maintaining old k8s cluster and designing a new one which is ready for production
    + all softwares managed by helm charts
    + less than one person per month for maintaining three clusters, which contains more than 300 services

### 天壤智能, Professional Data Processing Engineer(2019 - 2021)
* leading a team of 3-5 people
* designing and implementing a data processing platform named pandora, which based on flink, for analyzing advertisement putting data
    + refactoring business logic into small pieces with table and sql api of flink for better maintainability
    + reducing costs with flink on k8s
    + handling both batch and stream data with oss and kafka as the storage 
* refactoring pipelines and code of feature extracting and predicting algorithms for a recommendation system at China Merchants Bank
    + optimization for spark jobs
    + designing interfaces for algorithms
* main reasons to leave: my lovely daughter was coming, the business was given up and my boss left this company

### search dump team at Search Department Alibaba Group, Senior Java Developer(2015 - 2019)
* maintaining and developing data processing logic and search engine dump systems for multiple business lines
* designing and implementing an image data processing platform for image searching in Taobao: pailitao is the main user of this platform
    + establishing standards for c++ image processing modules, which based on protobuf, JNI, cmake, etc.
    + making standalone image processing modules, provided by algorithm engineers, to be a distributed system
    + handling modules like image feature extraction, image similarity calculation, clustering, etc.
    + keeping no online fault for more than 3 years after the platform was put into use
* maintaining a data processing platform for Taobao/Tmall main search
    + online fault not larger than P3
    + only two people, including me, handled the whole taobao/tmall data for searching for nearly 2 years
* maintaining data processing platforms for 1688, aliexpress, etc.
    + keeping no online fault
* kpi: 3.75 all the time except 3.5+(once)
* main reason to leave: deep dive into stream processing and k8s

### search dump team at Search Department Alibaba Group, intern(2014.07 - 2015.04)
* maintaining data processing platform for search engine
* Independently developed a distributed data synchronization tool, from MySQL to HBase, which was secure, high performance and widely used for more than 4 years

## papers published

* Song, Jie, HongYan He, Zhi Wang, Ge Yu, and Jean-Marc Pierson. "Modulo based data placement algorithm for energy consumption optimization of MapReduce system." Journal of Grid Computing 16 (2018): 409-424.
* 宋杰, 王智, 李甜甜, and 于戈. "一种优化 MapReduce 系统能耗的数据布局算法." 软件学报 26, no. 8 (2015): 2091-2110.
* Song, Jie, Tiantian Li, Zhi Wang, and Zhiliang Zhu. "Study on energy-consumption regularities of cloud computing systems by a novel evaluation model." Computing 95 (2013): 269-287.
* Song, Jie, Chaopeng Guo, Zhi Wang, Yichan Zhang, Ge Yu, and Jean-Marc Pierson. "HaoLap: A Hadoop based OLAP system for big data." Journal of Systems and Software 102 (2015): 167-181.
* 郭朝鹏, 王智, 韩峰, 张一川, and 宋杰. "HaoLap: 基于 Hadoop 的海量数据 OLAP 系统." 计算机研究与发展 S1 (2013): 378-383.
* 宋杰, 侯泓颖, 王智, and 朱志良. "云计算环境下改进的能效度量模型." 浙江大学学报: 工学版 1 (2013): 44-52.

## Education

* NorthEastern University (2009 - 2013), Bachelor's degree of Software Engineering
* NorthEastern University (2013 - 2015), Master's degree of Software Engineering

## Certifications

* English: Certificate VI
* [Red Hat Certified Engineer](RHCE_Zhi_Wang.pdf.md)
    + early in my high school: 2010.08.31

## additional

* Adept at solving problems in large-scale/distributed data processing systems
* Capable of designing and building search dump data processing platforms, CI/CD automation workflows, etc.
* Able to lead/mentor small product research and development team
* Can also develop microservice architectures using Spring Boot, but not an expert
* Can also use simple algorithm models, but not proficient
