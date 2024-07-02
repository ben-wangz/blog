# -*- coding: utf-8 -*-
import fsspec


def construct_filesystem(endpoint=None, access_key=None, secret_key=None):
    return fsspec.filesystem(
        "s3",
        key=access_key,
        secret=secret_key,
        client_kwargs={"endpoint_url": endpoint},
    )
