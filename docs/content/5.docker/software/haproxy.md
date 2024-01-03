### haproxy

* prepare haproxy.cfg
    + ```shell
      cat <<EOF > haproxy.cfg
      global
          log /dev/log local0
          log /dev/log local1 notice
      defaults
          log     global
          mode    tcp
          timeout connect 5000
          timeout client  50000
          timeout server  50000
      frontend http
          bind *:80
          bind *:443 ssl crt /usr/local/etc/haproxy/certs/
          mode http
          redirect scheme https code 301 if !{ ssl_fc }
          acl ACL_nginx ssl_fc_sni -i nginx.geekcity.tech
          use_backend nginx if ACL_nginx

          default_backend nodes

      backend nginx
          mode http
          server nginxServer host.docker.internal:8081
      backend nodes
          mode http
          balance roundrobin
          server defaultNginx host.docker.internal:8082
      EOF
      ```
* prepare pem files
    + ```shell
      mkdir -p $(pwd)/pem
      cat xxxxxx.pem xxxxxx.key > $(pwd)/pem/xxx.combined.pem
      ```
* ```shell
  podman run --rm -p 1443:443 -p 1080:80 \
      --add-host=host.docker.internal:host-gateway \
      -v $(pwd)/pem/:/usr/local/etc/haproxy/certs/:ro \
      -v $(pwd)/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro \
      -d docker.io/library/haproxy:2.2.14
  ```