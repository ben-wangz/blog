import requests
import fsspec

def from_http(url, s3_path, access_key, secret_key, endpoint):
    with fsspec.open(s3_path, "wb", s3={"key": access_key, "secret": secret_key, "client_kwargs": {"endpoint_url": endpoint}}) as s3_file:
        with requests.get(url, stream=True) as response:
            response.raise_for_status()
            for chunk in response.iter_content(chunk_size=8192):
                s3_file.write(chunk)
