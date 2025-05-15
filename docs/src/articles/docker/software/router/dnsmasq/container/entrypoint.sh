#! /bin/bash

set -e

WAN_INTERFACE=${WAN_INTERFACE:-ens18}
LAN_INTERFACE=${LAN_INTERFACE:-ens19}

DHCP_START_IP=${DHCP_START_IP:-192.168.10.50}
DHCP_END_IP=${DHCP_END_IP:-192.168.10.150}
DHCP_NETMASK=${DHCP_NETMASK:-255.255.255.0}
DHCP_LEASE_TIME=${DHCP_LEASE_TIME:-24h}

DHCP_GATEWAY=${DHCP_GATEWAY:-192.168.10.1}
DHCP_DNS=${DHCP_DNS:-$DHCP_GATEWAY}
DNS_SERVER=${DNS_SERVER:-223.5.5.5,223.6.6.6}

IFS=',' read -ra servers <<< "$DNS_SERVER"
SERVER_CONFIG=""
for server in "${servers[@]}"; do
    SERVER_CONFIG+="server=$server\n"
done

# example:
# export DHCP_STATIC_LEASES="00:11:22:33:44:55,192.168.10.101;AA:BB:CC:DD:EE:FF,192.168.10.102"
DHCP_STATIC_LEASES=${DHCP_STATIC_LEASES:-""}
STATIC_LEASE_CONFIG=""
IFS=';' read -ra leases <<< "$DHCP_STATIC_LEASES"
for lease in "${leases[@]}"; do
    if [ -n "$lease" ]; then
        IFS=',' read -ra parts <<< "$lease"
        mac="${parts[0]}"
        ip="${parts[1]}"
        STATIC_LEASE_CONFIG+="dhcp-host=$mac,$ip\n"
    fi
done

printf "interface=$LAN_INTERFACE\n" > /etc/dnsmasq.conf
printf "dhcp-range=$DHCP_START_IP,$DHCP_END_IP,$DHCP_NETMASK,$DHCP_LEASE_TIME\n" >> /etc/dnsmasq.conf
printf "dhcp-option=3,$DHCP_GATEWAY\n" >> /etc/dnsmasq.conf
printf "dhcp-option=6,$DHCP_DNS\n" >> /etc/dnsmasq.conf
printf "$SERVER_CONFIG" >> /etc/dnsmasq.conf
printf "$STATIC_LEASE_CONFIG" >> /etc/dnsmasq.conf
printf "no-resolv\n" >> /etc/dnsmasq.conf
printf "log-queries\n" >> /etc/dnsmasq.conf
printf "log-dhcp\n" >> /etc/dnsmasq.conf

mkdir -p /var/log/dnsmasq
echo "start dnsmasq with config:"
echo "--------------------------"
cat /etc/dnsmasq.conf
echo "--------------------------"
cat /etc/hosts
echo "--------------------------"
exec dnsmasq --no-daemon --conf-file=/etc/dnsmasq.conf
