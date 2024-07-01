import os
import http_to_s3

if __name__ == "__main__":
    access_key = os.environ.get("S3_ACCESS_KEY", "minioadmin")
    secret_key = os.environ.get("S3_SECRET_KEY", "minioadmin")
    endpoint = os.environ.get("S3_ENDPOINT", "http://host.containers.internal:9000" if os.environ.get("DEV_CONTAINER", "false") else "http://localhost:9000")
    bucket = os.environ.get("S3_BUCKET", "test")
    file_url = os.environ.get("FILE_URL", f"https://speed.cloudflare.com/__down?bytes={3*1024*1024}")
    s3_path = f"s3://{bucket}/foo/bar/zero.bin"

    print(f"uploading image from {file_url} to {s3_path}")
    http_to_s3.from_http(file_url, s3_path, access_key, secret_key, endpoint)
