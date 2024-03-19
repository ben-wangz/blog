# compress_fasttext

## Introduction

* allows to compress fastText word embedding models (from the gensim package) by orders of magnitude, without significantly affecting their quality

## prepare

1. prepare pre-trained model
    * ```shell
      curl -LO https://github.com/avidale/compress-fasttext/releases/download/gensim-4-draft/ft_cc.en.300_freqprune_400K_100K_pq_300.bin
      ```
2. prepare python script `vectorization.py`
    * ```python
      <!-- @include: vectorization.py -->
      ```

## run with container

```shell
podman run --rm \
    -v $(pwd)/ft_cc.en.300_freqprune_400K_100K_pq_300.bin:/app/model/ft_cc.en.300_freqprune_400K_100K_pq_300.bin \
    -v $(pwd)/vectorization.py:/app/vectorization.py \
    -e MODEL_PATH=/app/model/ft_cc.en.300_freqprune_400K_100K_pq_300.bin \
    -e SENTENCE="On a freezing New Year's Eve, a poor young girl, shivering, bareheaded and barefoot, unsuccessfully tries to sell matches in the street." \
    -it docker.io/library/python:3.12.1-bullseye \
        bash -c "pip install -i https://mirrors.aliyun.com/pypi/simple/ scikit-learn==1.4.0 compress-fasttext==0.1.4 && python3 /app/vectorization.py"
```

## reference

* https://github.com/avidale/compress-fasttext
