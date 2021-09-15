### software with docker

* phpmyadmin
    + ```shell
      docker run --rm -p 8080:80 -e PMA_ARBITRARY=1 -d phpmyadmin:5.1.1-apache
      ```
* nginx
    + ```shell
      docker run --rm -p 8080:80 \
          -v $(pwd)/data:/usr/share/nginx/html:ro \
          -v $(pwd)/default.conf:/etc/nginx/conf.d/default.conf:ro \
          -d nginx:1.19.9-alpine
      ```
* haproxy
    + ```shell
      cat > haproxy.cfg <<EOF
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
      acl ACL_nginx ssl_fc_sni -i proxy.geekcity.tech
      use_backend proxy if ACL_nginx
      default_backend nodes
      backend proxy
      mode http
      server proxy localhost:8081
      backend nodes
      mode http
      balance roundrobin
      server k8s_node1 localhost:8080
      EOF
      
      docker run --rm -p 443:443 -p 80:80 \
          -v $(pwd)/pem/:/usr/local/etc/haproxy/certs/:ro \
          -v $(pwd)/haproxy.cfg:/usr/local/etc/haproxy/haproxy.cfg:ro \
          -d haproxy:2.2.14
      ```
* mysqldump
    + backup databases
        * ```shell
          docker run -it mysql:8.0.25 mysqldump \
              -h database.aliyun.geekcity.tech \
              -P32306 \
              -uroot \
              -pAwdCXq87Ho \
              --all-databases \
              | gzip > db.sql.$(date +%s_%Y%m%d_%H_%M_%S).gz
          ```
    + import a database from another
        * ```shell
          // TODO
          mysqldump \
              -u root \
              -p database_name | mysql -h remote_host -u root -p remote_database_name
          ```
* v2fly

    + ```shell
      mkdir -p /opt/v2fly && cat > /opt/v2fly/config.json <<EOF
      {
        "inbounds": [
          {
            "port": 443,
            "protocol": "vmess",
            "settings": {
              "clients": [
                {
                  "id": "6daa6c46-25bd-4e56-8931-bb9d877a4190",
                  "alterId": 3103,
                  "security": "auto",
                  "level": 0
                }
              ]
            },
            "streamSettings": {
              "network": "tcp"
            }
          }
        ],
        "outbounds": [
          {
            "protocol": "freedom",
            "settings": {}
          }
        ]
      }
      EOF
      yum install -y yum-utils device-mapper-persistent-data lvm2 \
          && yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo \
          && yum -y install docker-ce \
          && systemctl enable docker \
          && systemctl start docker \
          && docker run --name v2fly --rm -p 8388:443/tcp -v /opt/v2fly/config.json:/etc/v2ray/config.json:ro -d v2fly/v2fly-core:v4.38.3
      ```