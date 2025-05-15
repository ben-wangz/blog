#! /bin/bash

set -e

IMAGE=${IMAGE:-localhost/router:latest}
WAN_INTERFACE=${WAN_INTERFACE:-ens18}
LAN_INTERFACE=${LAN_INTERFACE:-ens19}
DHCP_START_IP=${DHCP_START_IP:-192.168.10.50}
DHCP_END_IP=${DHCP_END_IP:-192.168.10.150}
DHCP_NETMASK=${DHCP_NETMASK:-255.255.255.0}
DHCP_LEASE_TIME=${DHCP_LEASE_TIME:-24h}
DHCP_DNS=${DHCP_DNS:-192.168.10.1}
DHCP_GATEWAY=${DHCP_GATEWAY:-192.168.10.1}
DHCP_STATIC_LEASES=${DHCP_STATIC_LEASES:-""}

if podman container exists dnsmasq; then
    podman stop dnsmasq
    podman rm dnsmasq
fi

podman run \
  --name dnsmasq \
  --restart=always \
  --cap-add=NET_ADMIN \
  -p 53:53/udp \
  -p 67:67/udp \
  -v /lib/modules:/lib/modules:ro \
  -e WAN_INTERFACE=$WAN_INTERFACE \
  -e LAN_INTERFACE=$LAN_INTERFACE \
  -e DHCP_START_IP=$DHCP_START_IP \
  -e DHCP_END_IP=$DHCP_END_IP \
  -e DHCP_NETMASK=$DHCP_NETMASK \
  -e DHCP_LEASE_TIME=$DHCP_LEASE_TIME \
  -e DHCP_DNS=$DHCP_DNS \
  -e DHCP_GATEWAY=$DHCP_GATEWAY \
  -e DHCP_STATIC_LEASES=$DHCP_STATIC_LEASES \
  -d $IMAGE
