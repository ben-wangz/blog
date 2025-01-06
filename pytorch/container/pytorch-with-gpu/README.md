# pytorch with gpu in container

## environment setup

1. create a gpu ECS from aliyun
    * more than 30GB disk space
    * install podman
        + ```shell
          apt-get update && apt-get install -y podman
          ```
2. install nvidia gpu driver
    * [reference](https://help.aliyun.com/zh/egs/user-guide/install-a-gpu-driver-on-a-gpu-accelerated-compute-optimized-linux-instance)
    * [reference]()
    * search for the gpu driver package in [nvidia website](https://www.nvidia.cn/drivers/lookup/)
        + choose: "Data Center / Tesla", "A-Series", "NVIDIA A10", "Linux 64-bit Ubuntu 24.04", "12.7" and "English(US)"
        + find the download link and download it
            * ```shell
              curl -LO https://us.download.nvidia.com/tesla/565.57.01/nvidia-driver-local-repo-ubuntu2404-565.57.01_1.0-1_amd64.deb
              ```
    * install this package
        + ```shell
          dpkg -i nvidia-driver-local-repo-ubuntu2404-565.57.01_1.0-1_amd64.deb
          cp /var/nvidia-driver-local-repo-ubuntu2404-565.57.01/nvidia-driver-local-685884D3-keyring.gpg /usr/share/keyrings/
          apt-get update && apt-get install -y nvidia-open
          apt-get update && apt-get install -y cuda-drivers
          ```
    * check the gpu driver
        + ```shell
          nvidia-smi
          ```
3. install `container-toolkit` for nvidia gpu
    * [reference](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html#installing-with-apt)
    * ```shell
      curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey | sudo gpg --dearmor -o /usr/share/keyrings/nvidia-container-toolkit-keyring.gpg \
        && curl -s -L https://nvidia.github.io/libnvidia-container/stable/deb/nvidia-container-toolkit.list | \
          sed 's#deb https://#deb [signed-by=/usr/share/keyrings/nvidia-container-toolkit-keyring.gpg] https://#g' | \
          sudo tee /etc/apt/sources.list.d/nvidia-container-toolkit.list
      sudo apt-get update
      sudo apt-get install -y nvidia-container-toolkit
      ```
4. install `cdi-support` for podman
    * [references](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/cdi-support.html)
    * ```shell
      nvidia-ctk cdi generate --output=/etc/cdi/nvidia.yaml
      nvidia-ctk cdi list
      podman run --rm --device nvidia.com/gpu=all --security-opt=label=disable docker.io/library/ubuntu:22.04 nvidia-smi -L
      ```
5. test environment with pytorch
    * prepare `pytorch-test.py`
        + ```python
          tee pytorch-test.py <<< "import torch
          print(torch.__version__)
          print(torch.cuda.is_available())"
          ```
    * run with podman
        + NOTES
            * this gpu driver only support cuda11.4(max version of cuda in ubuntu 24.04)
        + ```shell
          podman run --rm \
              --device nvidia.com/gpu=all \
              --security-opt=label=disable \
              -v $(pwd)/pytorch-test.py:/app/pytorch-test.py \
              -it docker.io/pytorch/pytorch:2.5.1-cuda12.4-cudnn9-devel \
                  python3 /app/pytorch-test.py
          ```

## demos

1. [classify handwritten digits](../../classify-handwritten-digits/README.md)
    * podman run with `--device nvidia.com/gpu=all` and `--security-opt=label=disable`
