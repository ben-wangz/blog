### v2fly

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