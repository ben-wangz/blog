# fasttext

## Introduction

* FastText is a library for efficient learning of word representations and sentence classification
* It allows users to learn text representations and text classifiers
* It is written in C++ and supports distributed training
* A popular idea in modern machine learning is to represent words by vectors. These vectors capture hidden information about a language, like word analogies or semantic. It is also used to improve performance of text classifiers.

## prepare

1. prepare pre-trained model
    * ```shell
      curl -LO https://dl.fbaipublicfiles.com/fasttext/vectors-wiki/wiki.en.zip
      unzip wiki.en.zip
      ```
2. prepare python script `vectorization.py`
    * ```python
      <!-- @include: vectorization.py -->
      ```

## run with container

```shell
# NOTE: need more than 16GB memory
podman run --rm \
    -v $(pwd)/wiki.en.bin:/app/model/wiki.en.bin \
    -v $(pwd)/vectorization.py:/app/vectorization.py \
    -e MODEL_PATH=/app/model/wiki.en.bin \
    -e SENTENCE="On a freezing New Year's Eve, a poor young girl, shivering, bareheaded and barefoot, unsuccessfully tries to sell matches in the street." \
    -it docker.io/library/python:3.12.1-bullseye \
        bash -c "pip install -i https://mirrors.aliyun.com/pypi/simple/ fasttext==0.9.2 && python3 /app/vectorization.py"
```

## reference

* https://github.com/facebookresearch/fastText
