ARG BASE_IMAGE=docker.io/library/python:3.12.1-bullseye
FROM $BASE_IMAGE

COPY requirements.txt /tmp/requirements.txt
RUN pip3 install --no-cache-dir -r /tmp/requirements.txt
COPY . /app
WORKDIR /app
CMD ["python3", "app.py"]
