FROM docker.io/library/python:3.12.1-bullseye

RUN pip install -i https://mirrors.aliyun.com/pypi/simple/ sentence-transformers==2.6.1
