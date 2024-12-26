# pytorch with gpu in container

## environment setup

1. create a gpu ECS from aliyun, let's take `ecs.sgn7i-vws-m2.xlarge` for example, which has `4 core` cpu, `16GB` memory and `1/12 * NVIDIA A10` gpu
    * OS: ubuntu 24.04 [default podman version is ]
    * install podman
        + ```shell
          apt-get update && apt-get install -y podman
          ```
2. install nvidia gpu driver(only for virtualized gpu of ecs)
    * [reference](https://help.aliyun.com/zh/egs/user-guide/use-cloud-assistant-to-automatically-install-and-upgrade-grid-drivers)
    * ```shell
      if acs-plugin-manager --list --local | grep grid_driver_install > /dev/null 2>&1
      then
          acs-plugin-manager --remove --plugin grid_driver_install
      fi
      acs-plugin-manager --exec --plugin grid_driver_install
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
4. install `cid-support` for podman
    * [references](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/cdi-support.html)
    * ```shell
      nvidia-ctk cdi generate --output=/etc/cdi/nvidia.yaml
      nvidia-ctk cdi list
      podman run --rm --device nvidia.com/gpu=all --security-opt=label=disable docker.io/library/ubuntu:22.04 nvidia-smi -L
      ```
5. test environment with pytorch
    * prepare `pytorch-test.py`
        + ```python
          import torch
          print(torch.__version__)
          print(torch.cuda.is_available())
          ```
    * run with podman
        + ```shell
          podman run --rm \
              --device nvidia.com/gpu=all \
              --security-opt=label=disable \
              -v $(pwd)/pytorch-test.py:/app/pytorch-test.py \
              -it docker.io/pytorch/pytorch:2.5.1-cuda11.8-cudnn9-devel \
                  python3 /app/pytorch-test.py
          ```

## classify-handwritten-digits



