### haproxy

* prepare [haproxy.cfg](resources/haproxy.cfg.md)
* prepare pem files
    + ```shell
      mkdir -p $(pwd)/pem
      cat xxxxxx.pem xxxxxx.key > $(pwd)/pem/xxx.combined.pem
      ```
* ```shell
  docker run --rm -p 443:443 -p 80:80 \
      --add-host=host.docker.internal:host-gateway \
      -v $(pwd)/pem/:/usr/local/etc/haproxy/certs/:ro \
      -v $(pwd)/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro \
      -d haproxy:2.2.14
  ```