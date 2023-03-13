# install haproxy

1. prepare [haproxy.cfg](resources/haproxy/haproxy.cfg.md)
2. install by docker
    * ```shell
      docker run --name haproxy \
          --rm -p 443:443 -p 80:80 \
          --add-host=host.docker.internal:host-gateway \
          -v $(pwd)/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro \
          -d haproxy:2.2.14
      ```