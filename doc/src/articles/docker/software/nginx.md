# nginx

* ```shell
  # prepare default.conf
  cat << EOF > default.conf
  server {
    listen 80;
    location / {
        root   /usr/share/nginx/html;
        autoindex on;
    }
  }
  EOF
  ```
* ```shell
  mkdir $(pwd)/data
  podman run --rm -p 8080:80 \
      -v $(pwd)/data:/usr/share/nginx/html:ro \
      -v $(pwd)/default.conf:/etc/nginx/conf.d/default.conf:ro \
      -d docker.io/library/nginx:1.19.9-alpine
  echo 'this is a test' > $(pwd)/data/some-data.txt
  ```
* visit http://localhost:8080
