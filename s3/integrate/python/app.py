# -*- coding: utf-8 -*-
import os
import requests
import s3_access_util
import fsspec


def construct_filesystem(endpoint=None, access_key=None, secret_key=None):
    return fsspec.filesystem(
        "s3",
        key=access_key,
        secret=secret_key,
        client_kwargs={"endpoint_url": endpoint},
    )


def stream_loading_from_http(url=None, s3_path=None, s3=None):
    with s3.open(s3_path, "wb") as s3_file:
        with requests.get(url, stream=True) as response:
            response.raise_for_status()
            for chunk in response.iter_content(chunk_size=8192):
                s3_file.write(chunk)


if __name__ == "__main__":
    access_key = os.environ.get("S3_ACCESS_KEY", "minioadmin")
    secret_key = os.environ.get("S3_SECRET_KEY", "minioadmin")
    endpoint = os.environ.get(
        "S3_ENDPOINT",
        (
            "http://host.containers.internal:9000"
            if os.environ.get("DEV_CONTAINER", "false")
            else "http://localhost:9000"
        ),
    )
    bucket = os.environ.get("S3_BUCKET", "test")

    s3 = s3_access_util.construct_filesystem(
        endpoint=endpoint, access_key=access_key, secret_key=secret_key
    )
    for index in range(0, 4):
        file_url = os.environ.get(
            "FILE_URL", f"https://speed.cloudflare.com/__down?bytes={index*1024*1024}"
        )
        s3_path = f"s3://{bucket}/foo/bar/zero-{index}.bin"
        print(f"uploading image from {file_url} to {s3_path}")
        stream_loading_from_http(file_url, s3_path, s3)
    s3.ls(f"s3://{bucket}/foo/bar")
    with s3.open(f"s3://{bucket}/foo/bar/zero-1.bin", "rb") as s3_file:
        print(s3_file.read(1024))
