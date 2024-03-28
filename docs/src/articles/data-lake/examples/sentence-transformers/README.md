# sentence-transformers

## Introduction

## prepare base image

* the dependency `sentence-transformers` is too big to install, so we need to prepare a base image with it installed
* prepare `base.dockerfile`
    + ```text
      <!-- @include: base.dockerfile -->
      ```
* build the base image
    + ```shell
      podman build -t sentence-transformers-base:latest -f base.dockerfile .
      ```

## prepare pre trained model
* the models may be too big to download again and again, so we need to prepare the model in advance
* prepare `save-model.py`
    + ```python
      <!-- @include: save-model.py -->
      ```
* run with container
    + ```shell
      mkdir -p models
      podman run --rm \
          -v $(pwd)/save-model.py:/app/save-model.py \
          -v $(pwd)/models:/app/models \
          -e MODEL_NAME=clip-ViT-B-32 \
          -e MODEL_PATH=/app/models/clip-ViT-B-32 \
          -it sentence-transformers-base:latest \
              python3 /app/save-model.py
      podman run --rm \
          -v $(pwd)/save-model.py:/app/save-model.py \
          -v $(pwd)/models:/app/models \
          -e MODEL_NAME=sentence-transformers/clip-ViT-B-32-multilingual-v1 \
          -e MODEL_PATH=/app/models/sentence-transformers/clip-ViT-B-32-multilingual-v1 \
          -it sentence-transformers-base:latest \
              python3 /app/save-model.py
      ```

## similarity with text and image
1. prepare `similarity.py`
    * ```python
      <!-- @include: similarity.py -->
      ```
2. run with container
    * ```shell
      podman run --rm \
          -v $(pwd)/similarity.py:/app/similarity.py \
          -v $(pwd)/models:/app/models \
          -e IMAGE_MODEL_PATH=/app/models/clip-ViT-B-32 \
          -e TEXT_MODEL_PATH=/app/models/sentence-transformers/clip-ViT-B-32-multilingual-v1 \
          -it sentence-transformers-base:latest \
              python3 /app/similarity.py
      ```

## reference

* https://github.com/UKPLab/sentence-transformers
* https://huggingface.co/sentence-transformers/clip-ViT-B-32-multilingual-v1
