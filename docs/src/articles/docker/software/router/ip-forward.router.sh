#! /bin/bash

set -e

WAN_INTERFACE=${WAN_INTERFACE:-ens18}
LAN_INTERFACE=${LAN_INTERFACE:-ens19}

echo 1 > /proc/sys/net/ipv4/ip_forward

# clean up previously added rules
iptables -t nat -D POSTROUTING -o $WAN_INTERFACE -j MASQUERADE 2>/dev/null || true
iptables -D FORWARD -i $WAN_INTERFACE -o $LAN_INTERFACE -m state --state RELATED,ESTABLISHED -j ACCEPT 2>/dev/null || true
iptables -D FORWARD -i $LAN_INTERFACE -o $WAN_INTERFACE -j ACCEPT 2>/dev/null || true

# add new rules
iptables -t nat -A POSTROUTING -o $WAN_INTERFACE -j MASQUERADE
iptables -A FORWARD -i $WAN_INTERFACE -o $LAN_INTERFACE -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -A FORWARD -i $LAN_INTERFACE -o $WAN_INTERFACE -j ACCEPT
