# let's encrypt

## references

* https://letsencrypt.org/getting-started/
* https://github.com/acmesh-official/acme.sh/wiki/dnsapi#11-use-aliyun-domain-api-to-automatically-issue-cert
* https://github.com/acmesh-official/acme.sh/wiki/Run-acme.sh-in-docker

## prerequisites

1. prepare RAM access policy to your aliyun account
    * create policy
        + ```json
          {
              "Version": "1",
              "Statement": [
                  {
                      "Effect": "Allow",
                      "Action": [
                          "alidns:AddDomainRecord",
                          "alidns:DeleteDomainRecord"
                      ],
                      "Resource": "acs:alidns:*:*:domain/geekcity.tech"
                  },
                  {
                      "Effect": "Allow",
                      "Action": [
                          "alidns:DescribeDomains",
                          "alidns:DescribeDomainRecords"
                      ],
                      "Resource": "acs:alidns:*:*:domain/*"
                  }
              ]
          }
          ```
2. assuming we have an account with `AccessKey` and `AccessSecret`
    + ```shell
      Ali_Key=your_aliyun_key
      Ali_Secret=your_aliyun_secret
      ```
3. add policy to your account

## issue a certificate

* assuming we have a domain `geekcity.tech`
* issue a certificate by letsencrypt
    + ```shell
      mkdir -p output
      podman run --rm \
        -v "$(pwd)/output":/acme.sh \
        -e Ali_Key=$Ali_Key \
        -e Ali_Secret=$Ali_Secret \
        docker.io/neilpang/acme.sh --issue --dns dns_ali -d test.letsencrypt.geekcity.tech --server letsencrypt
      ```
    + the outputs will be in the `output` directory

## tests

1. install the certificate into haproxy
    * ```shell
      mkdir -p haproxy/pem
      cat $(pwd)/output/test.letsencrypt.geekcity.tech_ecc/test.letsencrypt.geekcity.tech.cer \
        $(pwd)/output/test.letsencrypt.geekcity.tech_ecc/test.letsencrypt.geekcity.tech.key \
        > $(pwd)/haproxy/pem/test.letsencrypt.geekcity.tech.pem
      ```
2. run the haproxy
    * ```shell
      cat <<EOF > haproxy/haproxy.cfg
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
          acl ACL_nginx ssl_fc_sni -i test.letsencrypt.geekcity.tech
          use_backend nginx if ACL_nginx
          default_backend nodes
      backend nginx
          mode http
          server nginxServer host.containers.internal:8080
      backend nodes
          mode http
          balance roundrobin
          server defaultNginx host.containers.internal:8888
      EOF
      ```
    * ```shell
      podman run --rm -p 1443:443 -p 1080:80 \
        -v $(pwd)/haproxy/pem:/usr/local/etc/haproxy/certs/:ro \
        -v $(pwd)/haproxy/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro \
        -d docker.io/library/haproxy:2.2.14
      ```
3. setup nginx to test
    * ```shell
      mkdir -p nginx
      # prepare default.conf
      cat << EOF > $(pwd)/nginx/default.conf
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
      mkdir $(pwd)/nginx/data
      podman run --rm -p 8080:80 \
          -v $(pwd)/nginx/data:/usr/share/nginx/html:ro \
          -v $(pwd)/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro \
          -d docker.io/library/nginx:1.19.9-alpine
      echo 'this is a test' > $(pwd)/nginx/data/some-data.txt
      ```
4. point `127.0.0.1` to `test.letsencrypt.geekcity.tech`
    * ```shell
      echo '127.0.0.1 test.letsencrypt.geekcity.tech' >> /etc/hosts
      ```
5. visit with curl
    * ```shell
      curl -v https://test.letsencrypt.geekcity.tech:1443
      ```
