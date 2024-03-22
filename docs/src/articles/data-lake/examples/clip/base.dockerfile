FROM docker.io/library/python:3.12.1-bullseye

RUN pip install -i https://mirrors.aliyun.com/pypi/simple/ open-clip-torch==2.24.0
