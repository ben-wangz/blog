# projects

## astronomy center of ZhejiangLab

## CSST

### introduction

* website: https://astro-workbench.zero2x.org/
* hand manual: https://www.yuque.com/tianpin-aqb6e/hdwuhf/kicx3mgllp84ga77
* The Astronomical Scientific Research Workstation is a universal astronomical data computing platform driven by the China Space Station Telescope (CSST). Its core objective is to establish the CSST Data Science Center. The platform integrates CSST data and provides user interfaces to assist researchers in managing data processing pipelines, while also offering a suite of intelligent agent tools to support astronomical teams in conducting CSST pre-research work. Beyond supporting the CSST mission, the platform also incorporates FAST scientific data and related domain models, introduces knowledge bases in areas such as astrochemistry, fast radio bursts, transient sources, and neutral hydrogen, and integrates a multi-wavelength data fusion platform to provide support for broader astronomical scientific research.
* example images
    + ![workflow-management](images/workflow-management.png)

### techniques provided by us

1. metadata
2. storage
3. star catalog
4. workflows
5. slurm on k8s
6. codespace
7. cicd: actions and standard service
8. middleware softwares
9. monitoring

## FAST

### introduction

The Five-hundred-meter Aperture Spherical radio Telescope (FAST), known as "China Sky Eye", is the world's largest single-dish radio telescope. Since 2019, FAST generates nearly 100PB of scientific data annually.

**FAST@LAB Scientific Data Processing Platform** addresses astronomical big data challenges with focus on:
* **Scientific Objectives**: Pulsars and fast radio bursts, neutral hydrogen spectral observations
* **Platform Goals**: Standardized multi-level data products and processing workflows
* **Architecture**: Intelligent computing platform integrating data management, model development, task scheduling, and GPU/CPU clusters

**Key Data Products**:
1. **BlinkVerse** - World's most comprehensive FRB database (789 FRBs, 99% coverage) with AI-assisted analysis
2. **Chemiverse** - Astrochemical reaction network (10,624 reactions, 1,004 species)
3. **TransientVerse** - Transient source knowledge base with AI literature assistance

**Scientific Algorithms**:
* FAST-FREX: Standard FRB dataset for algorithm benchmarking
* RaSPDAM/DRAFTS: FRB/PSR detection algorithms using visual features and AI
* PrestoZL: GPU-accelerated Presto (14-30x speedup)
* HI Pipeline: RFI removal and HI source finding

### techniques provided by us

1. metadata
2. storage
3. workflows
4. cicd: actions and standard service
5. middleware softwares
6. monitoring

## LHAASO

### introduction

### techniques provided by us

1. storage
2. workflows
3. codespace

## cnSRC@SRCNet of SKA

### introduction

### techniques provided by us

1. k8s environment
2. storage
3. workflows
4. monitoring

## OneAstronomy

### introduction

**OneAstronomy** is an AI-powered astronomical domain model developed by ZhejiangLab's Astronomical Computing Research Center to accelerate scientific discovery in the AI era.

**Model Highlights**:
* **Multi-modal**: Supports images, spectra, and time-domain data across radio to γ-ray wavelengths
* **Global Access**: 5 continents, 1934+ astronomers visiting; API integration with universe exploration system

**Platform Architecture**:
* **Data**: 30M papers, scientific databases (BlinkVerse, Chemiverse, TransientVerse), Google Scholar integration
* **Models**: O21 foundation model + specialized astronomical models (LHAASO, SpecCLIP, FALCO, FAST, AstroChem, Solar)
* **AI Systems**: DeepSeek/Qwen2.5 integration for AI scientist and embodied intelligence

**Key Capabilities**:
1. Precise astronomical domain knowledge with Q&A and reasoning
2. Real-time knowledge updates via RAG (45M+ papers, 10GB+)
3. Multi-wavelength data analysis from FAST, SKA, ALMA, CSST, LAMOST, LHAASO
4. Scientific problem coverage: cosmology, stellar systems, high-energy physics, exoplanets, HI evolution

**Data Products**: BlinkVerse (FRB database), Chemiverse (astrochemical reactions), TransientVerse (transient sources)

**Website**: https://oneastronomy.zero2x.org/

### techniques provided by us

1. dify with dependencies
2. RAG system with vector database (Milvus) and knowledge graph
