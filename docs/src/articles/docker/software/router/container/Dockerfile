FROM docker.io/library/ubuntu:24.04

RUN apt-get update \
  && apt-get -y install iproute2 iptables dnsmasq \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

COPY --chmod=0744 entrypoint.sh /entrypoint.sh
CMD ["/entrypoint.sh"]
