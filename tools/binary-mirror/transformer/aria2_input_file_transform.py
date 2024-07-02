import os
from urllib.parse import urlparse

download_path = os.environ.get("DOWNLOAD_PATH", "/data/download")
input_file_path = os.environ.get("INPUT_FILE_PATH", "/data/files.list")
output_file_path = os.environ.get("OUTPUT_FILE_PATH", "/data/files.list.aria2")
with open(output_file_path, "w") as output_file:
    with open(input_file_path) as input_file:
        for line in input_file:
            url = line.strip()
            url_parsed = urlparse(url)
            netloc = url_parsed.netloc
            filename = os.path.basename(url_parsed.path)
            path = os.path.dirname(url_parsed.path)
            output_file.write(url + "\n")
            output_file.write(
                " dir=" + os.path.join(download_path, netloc, path.strip("/")) + "\n"
            )
            output_file.write(" out=" + filename + "\n")
