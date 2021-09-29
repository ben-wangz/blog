### nginx

* prepare [default.conf](resources/default.conf.md)
* ```shell
  docker run --rm -p 8080:80 \
      -v $(pwd)/data:/usr/share/nginx/html:ro \
      -v $(pwd)/default.conf:/etc/nginx/conf.d/default.conf:ro \
      -d nginx:1.19.9-alpine
  ```
* visit http://localhost:8080
