ARG BASE_IMAGE=docker.io/pytorch/pytorch:2.5.1-cuda12.4-cudnn9-devel
FROM ${BASE_IMAGE}
COPY . /app/classify-handwritten-digits
ARG PYPI_MIRROR=https://pypi.python.org/simple
WORKDIR /app/classify-handwritten-digits
RUN pip3 install -r requirements.txt -i ${PYPI_MIRROR}
