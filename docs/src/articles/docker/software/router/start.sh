#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

IMAGE=${IMAGE:-localhost/router:latest}

ROUTER_GATEWAY=${ROUTER_GATEWAY:-192.168.10.1}
ROUTER_SUBNET=${ROUTER_SUBNET:-192.168.10.0/24}
WAN_INTERFACE=${WAN_INTERFACE:-ens18}
LAN_INTERFACE=${LAN_INTERFACE:-ens19}
DHCP_START_IP=${DHCP_START_IP:-192.168.10.50}
DHCP_END_IP=${DHCP_END_IP:-192.168.10.150}
DHCP_NETMASK=${DHCP_NETMASK:-255.255.255.0}
DHCP_LEASE_TIME=${DHCP_LEASE_TIME:-24h}
DHCP_DNS=${DHCP_DNS:-$ROUTER_GATEWAY}
DHCP_GATEWAY=${DHCP_GATEWAY:-$ROUTER_GATEWAY}
DHCP_STATIC_LEASES=${DHCP_STATIC_LEASES:-""}

echo 1 > /proc/sys/net/ipv4/ip_forward
podman run \
  --name router \
  --cap-add=NET_ADMIN \
  --cap-add=NET_RAW \
  --net=host \
  --restart=always \
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
  --device=/dev/net/tun \
  --device=/dev/vhost-net \
  -d $IMAGE
