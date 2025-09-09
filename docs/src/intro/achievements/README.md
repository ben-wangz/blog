# Achievements

## Storage

We have built a PB-scale distributed storage system that not only achieves efficient storage and read/write of massive data but also integrates powerful data retrieval and management functions. While ensuring massive data storage capabilities, it provides one-stop data lifecycle management services.

### PB-scale Distributed Storage Capabilities

At the storage system layer, the solution delivers POSIX compatibility through FUSE-based mounting at the operating system level, enabling PB-scale horizontal scalability. Business applications remain largely unaware of underlying storage scaling and routine maintenance activities. The system integrates two enterprise-grade distributed storage systems—Ceph and JuiceFS—to achieve a hybrid storage architecture that:
- Maximizes local cluster storage potential with near-data processing capabilities, significantly enhancing computational efficiency
- Leverages external object storage resources (S3/OSS) for cost-effective storage expansion, providing flexible support for large-scale data requirements

#### Ceph on K8s as High-stability, Low-latency Storage Support

The solution leverages internal storage across cluster nodes to deploy a Ceph storage cluster, utilizing Ceph's CRUSH algorithm to enable distributed data storage with triple-replica redundancy. In optimally sized Ceph clusters, this architecture achieves 99.9999% overall data availability while supporting dynamic scaling and autonomous data fault recovery mechanisms.

When replica counts drop below predefined safety thresholds, the system automatically activates write protection mechanisms to preserve data consistency and system stability. Performance metrics show significant improvements in IOPS compared to single-node storage configurations, with throughput scaling linearly as the number of Ceph OSDs increases.

By co-locating storage and compute resources within the same Kubernetes cluster, this implementation effectively supports mission-critical workloads requiring high storage stability and low-latency I/O performance, including real-time data processing and core database storage scenarios.

#### JuiceFS as Low-cost Elastic Expansion Storage Supplement

Ceph storage capacity is constrained by the internal storage of cluster physical nodes, making it ideal for mission-critical workloads with high priority but relatively modest capacity requirements. By integrating the JuiceFS file system and connecting to external object storage (S3/OSS), this architecture transcends local storage limitations. A single JuiceFS file system can theoretically scale to exabyte-level capacity while leveraging local SSD disks for caching to maintain high concurrent read/write performance.

Throughput is dependent on the network bandwidth between the cluster and the S3/OSS backend, creating a complementary storage solution that addresses Ceph's capacity constraints. This makes JuiceFS particularly well-suited for large-scale, cost-effective cold data storage scenarios, such as historical scientific research data archiving.

#### Standardized Storage Access

Built on the Kubernetes Container Storage Interface (CSI), this architecture integrates the JuiceFS and Ceph distributed storage systems, enabling both to deliver highly available, scalable storage services to virtually all workloads within the Kubernetes cluster through standard interfaces like Persistent Volume Claims (PVCs). This standardized approach facilitates flexible storage allocation and efficient resource utilization across the platform.

Key capabilities include dynamic, on-demand storage provisioning that enables workloads to access storage resources precisely when needed. The solution supports hot expansion via elastic scaling mechanisms during periods of resource constraints, while shared storage pools promote cross-workload resource reuse and optimization through oversubscription techniques, resulting in significant reductions in overall storage costs.

Beyond these core capabilities, the CSI-based storage architecture delivers several additional strategic advantages:

- **Storage Portability**: By abstracting underlying storage implementations behind standard interfaces, the solution enables seamless migration of workloads and their associated data between different storage providers or cluster environments without application modification.
- **Simplified Operations and Maintenance**: The unified interface reduces operational complexity by enabling a consistent management experience for administrators across diverse storage technologies, streamlining tasks such as monitoring, troubleshooting, and capacity planning.
- **Future-Proof Architecture**: Leveraging Kubernetes' CSI standard ensures compatibility with emerging storage technologies and innovations, allowing organizations to adopt new storage solutions without disrupting existing workflows or requiring application changes.

### Data Management Capabilities

Our data management capabilities prioritize robust data retrieval functionality, which is categorized into two primary types: metadata-based retrieval and detailed data retrieval.

Metadata-based retrieval enables rapid identification and location of target datasets through predefined structured data attributes, including but not limited to data creation timestamps, data types, business domains, and data ownership information.

Detailed data retrieval supports deep exploration of specific data content, such as extracting information containing targeted keywords, semantic context, or patterns from structured data sources as well as unstructured formats like text, images, and audio. This is accomplished through diverse query mechanisms including SQL statements, natural language queries, and vector similarity computations.

The metadata retrieval capability is delivered through our proprietary metadata management service, while detailed data retrieval is implemented by users leveraging our distributed database middleware solution.

#### Self-developed Metadata Management Service

Leveraging a declarative API, users manage metadata collection behavior by defining the desired target state, with the system continuously monitoring and reconciling discrepancies between the current operational state and this predefined target. Upon detecting deviations in metadata collection task configurations—including parameters, execution cycles, or data source connectivity—the system autonomously orchestrates resource provisioning, updates, or decommissioning as required.

The metadata retrieval engine is powered by ElasticSearch, delivering millisecond-latency query performance. For metadata collection and persistence, we employ a hybrid streaming-batch architecture built on Kafka and Flink, featuring elastic horizontal scaling capabilities that linearly enhance data processing throughput. This architecture ensures both real-time responsiveness and high availability throughout the metadata lifecycle.

Beyond standard capabilities, metadata collection tasks support highly extensible custom extraction logic. Users can embed proprietary code to perform deep content analysis and precise metadata extraction from diverse file formats. This customization framework delivers significant advantages for specialized data types: for FITS astronomical data, it enables efficient parsing of header metadata describing file characteristics; for columnar storage formats like Parquet, it facilitates accurate schema extraction—substantially improving the system's adaptability and flexibility across varied data processing scenarios.

#### Distributed Database Middleware

While the platform does not support direct fine-grained retrieval across all file types, we address the diverse query requirements of structured, unstructured, and semi-structured data through a comprehensive distributed database ecosystem. This ecosystem includes:
- TiDB (relational and key-value database)
- ElasticSearch (full-text search database)
- MongoDB (document database)
- Milvus (vector database)
- ... (other database solutions)

To enable performant and precise query operations, users are advised to migrate their data to the database solution that best aligns with their specific use case and data characteristics.

## Compute

Built on the Kubernetes ecosystem, we have established a sophisticated, multi-component computing framework integrating Argo Workflow (for DAG-dependent job orchestration), Flink on K8s (for stream-batch unified big data processing), and Slurm on K8s (for high-performance computing compatibility). This comprehensive system supports ultra-large-scale data processing pipelines, hybrid real-time/offline data workloads, and multi-node collaborative computing scenarios—delivering optimized task scheduling, efficient execution, and maximized computing resource utilization.

### DAG-Dependent Job Scheduling (Argo Workflow)

We have developed an advanced batch task orchestration framework built on Argo Workflow, empowering users to flexibly model complex Directed Acyclic Graph (DAG) dependencies between tasks—for example, supporting common machine learning pipeline sequences such as data cleaning → feature extraction → model training → result evaluation. Through the provision of container images, execution commands, and explicit definition of task dependency topologies via declarative configuration, users can achieve high-efficiency orchestration and parallel processing of thousands of concurrent tasks within a Kubernetes cluster environment.

A robust task fault-tolerance mechanism has been implemented, featuring configurable retry policies (supporting custom retry counts and intervals) and checkpoint-based resumption capabilities. This architecture effectively minimizes the risk of complete task disruption due to individual subtask failures, resulting in significant improvements in task execution efficiency and resource utilization compared to traditional processing approaches.

### Stream-Batch Integrated Big Data Processing (Flink on K8s)

We have implemented a Flink on Kubernetes cluster supporting unified stream-batch processing mode, enabling concurrent handling of real-time streaming data (including real-time feature extraction and real-time monitoring data computation) and offline batch data (such as historical data statistical analysis). This system delivers industry-leading performance with data processing latency ≤ 1 second and daily throughput capacity of multiple terabytes.

By harnessing Kubernetes' elastic scaling capabilities, the Flink cluster dynamically adjusts the number of TaskManagers in response to workload volume, minimizing resource wastage and achieving significant improvements in resource utilization efficiency. This architecture effectively fulfills the hybrid "real-time + offline" data processing requirements across both research and business scenarios.

### High-Performance Computing Compatibility (Slurm on Kubernetes)

We have implemented an advanced Slurm on Kubernetes cluster that delivers comprehensive compatibility with mainstream High-Performance Computing (HPC) workloads. This architecture enables users to seamlessly submit both Slurm script tasks and MPI (Message Passing Interface) parallel computations, effectively addressing the demands of multi-node collaborative computing scenarios—such as large-scale parallel processing of astronomical datasets (e.g., radio signal analysis) and complex particle physics simulations.

Leveraging Kubernetes' unified resource scheduling framework, we have implemented optimized resource management for high-performance computing workloads like Slurm, enabling dynamic allocation of physical CPU, GPU, memory, and storage resources. A key innovation is the shared GPU scheduling capability, which dramatically enhances resource utilization. Through technologies including time-sharing multiplexing and virtual slicing, a single GPU card can concurrently support multiple AI tasks, achieving significantly higher GPU utilization compared to traditional exclusive allocation models.

## Monitor

### Multi-Dimensional Monitoring System

We have established a comprehensive monitoring system that covers three core dimensions: cluster infrastructure, middleware components, and business applications. At the cluster layer, we monitor key metrics of Kubernetes nodes and pods, including CPU usage, memory utilization, disk I/O, and network throughput, ensuring the stable operation of the underlying infrastructure. For middleware, we implement specialized monitoring for Flink, Slurm, Spark, and other data processing frameworks, tracking job execution status, resource consumption, and performance bottlenecks in real-time. On the business side, we collect and analyze application-specific metrics such as task completion rates, data processing latency, and service availability to ensure the quality and efficiency of business operations.

### Business-Driven Visualization Dashboard

Based on our extensive experience in data processing and high-performance computing scenarios, we have developed a business-oriented visualization dashboard. This dashboard provides intuitive visual representations of complex technical metrics and business KPIs, enabling users to quickly grasp the overall health and operational status of the system. Key features include customizable monitoring views, real-time data refresh, and drill-down analysis capabilities. Users can create personalized dashboards according to their roles and business needs, such as focusing on resource allocation efficiency for administrators or monitoring specific job performance for data scientists. The dashboard also supports historical data querying and trend analysis, helping teams identify potential issues in advance and make informed decisions for capacity planning.

### Log-Based Intelligent Alert System (Experimental)

We have developed an experimental log-based alert system that leverages log aggregation, correlation analysis, and basic machine learning approaches to explore anomaly detection and issue identification capabilities. This prototype system collects and normalizes logs from various components across the cluster, including application logs, system logs, and container logs. Initial testing has focused on establishing alert rules based on simple patterns and static thresholds to identify basic abnormal behaviors such as sudden increases in error rates, unexpected resource spikes, or service downtime.

*Note: This system is currently in the experimental phase and has not been deployed to production environments. While preliminary results show potential for reducing mean time to detect (MTTD) and mean time to resolve (MTTR) issues, further development and validation are required before it can be utilized in real-world data processing and computing infrastructure.*

## Collaborative Development

### Code-Centered Development Environment

We have established a code-centric collaborative development system with all our code repositories hosted on our self-built Gitea platform. This centralized code management approach ensures consistent version control practices, enhances code security, and provides a unified platform for team collaboration across all projects.

### Advanced Codespace Operator

We have developed an innovative Codespace Operator that enables on-demand creation of development environments based on any code repository. Users can initiate a development environment by submitting a Custom Resource (CR), with environment configurations automatically pulled from the `.codespace` directory within the target repository. Once provisioned, developers can seamlessly connect to their codespace using either local IDEs like VS Code (via secure remote connection) or directly through the web-based VS Code interface.

The operator also supports advanced features such as Podman-in-Container functionality, allowing developers to run containerized workloads within their isolated development environment. This approach delivers three significant benefits:
- **Unified Development Environment**: Ensures consistency across all team members' development setups, eliminating "works on my machine" issues
- **Enhanced Collaboration**: Facilitates concurrent development on shared codebases with isolated environments
- **Resource Optimization**: Enables resource oversubscription, particularly through GPU sharing capabilities, resulting in substantial resource savings

### GitOps and CI/CD Integration

Leveraging Gitea's extensibility, we have implemented GitHub Actions-compatible workflows that provide comprehensive GitOps capabilities. This integration unifies our CI/CD toolchain, streamlining build, test, and deployment processes while significantly reducing management overhead. By automating these workflows through Git-based operations, we ensure greater consistency, traceability, and reliability in our software delivery pipeline.

### ArgoCD-Powered Application Management

We utilize ArgoCD as our primary tool for managing all applications across our infrastructure. This GitOps-centric approach ensures that the desired state of applications is defined and version-controlled in Git repositories, with ArgoCD continuously reconciling the actual state with the desired state. ArgoCD provides essential capabilities such as automated deployments, real-time application status monitoring, and robust rollback mechanisms that enable quick recovery from deployment anomalies. This approach has greatly improved the reliability and resilience of our application delivery process.

## VM Provisioning and Management

TODO ...

## Algorithms

### Labeling

TODO ...

### Training

TODO ...

### Inference

We have developed a comprehensive inference service module built on KServe, which provides a robust, scalable, and efficient platform for deploying and managing AI models in production environments.

#### Elastic Scaling Capabilities

Leveraging KNative Serving, we have implemented a dynamic resource scheduling mechanism that enables "scale-to-zero" functionality. This allows the system to automatically release 100% of idle resources when there are no inference requests, significantly reducing infrastructure costs. When requests arrive, the system can quickly spin up services within a short period. This design perfectly accommodates the "tidal" computational demands of AI inference workloads, leading to substantial improvements in resource utilization efficiency.

#### High Throughput Performance

To achieve high throughput, we have implemented several key optimizations:
- **Asynchronous IO Mechanism**: Converting data read/write operations to non-blocking mode to prevent threads from blocking while waiting for IO completion
- **Message Middleware Integration**: Building asynchronous processing queues using message middleware to leverage its peak-shaving and valley-filling characteristics, distributing burst traffic evenly to backend processing units and effectively alleviating high concurrency pressure
- **Shared Resource Pool**: Implementing elastic allocation and reuse of scarce resources such as GPUs and storage, increasing AI inference throughput without expanding the total resource pool

#### Low-Cost Access

We have significantly lowered the barrier to entry for deploying inference services by:
- Encapsulating underlying logic including load balancing, service discovery, and model version management
- Providing standardized inference service configuration templates
- Enabling users to deploy inference services simply by uploading model files and inference scripts (after completing single-container configuration)

This approach has dramatically reduced the onboarding period for novice users and significantly lowered the engineering threshold for inference services.

### Classic Algorithms

TODO ...
