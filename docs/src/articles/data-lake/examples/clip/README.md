# CLIP

## Introduction

## prepare pre trained model

```shell
curl -LO https://huggingface.co/laion/CLIP-ViT-B-16-laion2B-s34B-b88K/resolve/main/open_clip_pytorch_model.bin
```

## prepare base image

* the dependency `open-clip-torch` is too big to install, so we need to prepare a base image with it installed
* prepare `base.dockerfile`
    + ```text
      <!-- @include: base.dockerfile -->
      ```
* build the base image
    + ```shell
      podman build -t open-clip-base:latest -f base.dockerfile .
      ```

## Text Vectorization
1. prepare `text-vectorization.py`
    * ```python
      <!-- @include: text-vectorization.py -->
      ```
2. run with container
    * ```shell
      podman run --rm \
          -v $(pwd)/open_clip_pytorch_model.bin:/app/model/open_clip_pytorch_model.bin \
          -v $(pwd)/text-vectorization.py:/app/text-vectorization.py \
          -e MODEL_PATH=/app/model/open_clip_pytorch_model.bin \
          -e MODEL_NAME=ViT-B-16 \
          -e SENTENCE="On a freezing New Year's Eve, a poor young girl, shivering, bareheaded and barefoot, unsuccessfully tries to sell matches in the street." \
          -it localhost/clip-test:latest \
              python3 /app/text-vectorization.py
      ```

## Image Vectorization
1. prepare `image-vectorization.py`
    * ```python
      <!-- @include: image-vectorization.py -->
      ```
2. prepare `image.jpg`
    * ![image.jpg](image.jpg)
3. run with container
    * ```shell
      podman run --rm \
          -v $(pwd)/open_clip_pytorch_model.bin:/app/model/open_clip_pytorch_model.bin \
          -v $(pwd)/image-vectorization.py:/app/image-vectorization.py \
          -v $(pwd)/image.jpg:/app/image.jpg \
          -e MODEL_PATH=/app/model/open_clip_pytorch_model.bin \
          -e MODEL_NAME=ViT-B-16 \
          -e IMAGE_PATH=/app/image.jpg \
          -it localhost/clip-test:latest \
              python3 /app/image-vectorization.py
      ```

## reference

* https://github.com/openai/CLIP
* https://github.com/mlfoundations/open_clip
* https://huggingface.co/laion/CLIP-ViT-B-16-laion2B-s34B-b88K
