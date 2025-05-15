#!/bin/bash

set -e

# set up NAT port forwarding
# example: setup_nat_port_forward "ens18" "192.168.10.100" 80 8080
setup_nat_port_forward() {
    local WAN_INTERFACE="${1}"
    local LOCAL_IP="${2}"
    local LOCAL_PORT="${3}"
    local EXTERNAL_PORT="${4}"

    if [ -z "$WAN_INTERFACE" ] || [ -z "$LOCAL_IP" ] || [ -z "$LOCAL_PORT" ] || [ -z "$EXTERNAL_PORT" ]; then
        echo "Error: WAN_INTERFACE, LOCAL_IP, LOCAL_PORT, and EXTERNAL_PORT parameters must be provided."
        return 1
    fi

    # remove previously added port forwarding rules
    local WAN_IP=$(ip -4 addr show $WAN_INTERFACE | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
    iptables -t nat -D PREROUTING -i $WAN_INTERFACE -p tcp --dport $EXTERNAL_PORT -j DNAT --to-destination $LOCAL_IP:$LOCAL_PORT 2>/dev/null || true
    iptables -t nat -D POSTROUTING -d $LOCAL_IP -p tcp --dport $LOCAL_PORT -j SNAT --to-source $WAN_IP 2>/dev/null || true
    iptables -D FORWARD -i $WAN_INTERFACE -p tcp --dport $LOCAL_PORT -d $LOCAL_IP -j ACCEPT 2>/dev/null || true

    # set new port forwarding rules
    iptables -t nat -A PREROUTING -i $WAN_INTERFACE -p tcp --dport $EXTERNAL_PORT -j DNAT --to-destination $LOCAL_IP:$LOCAL_PORT
    iptables -t nat -A POSTROUTING -d $LOCAL_IP -p tcp --dport $LOCAL_PORT -j SNAT --to-source $WAN_IP
    iptables -A FORWARD -i $WAN_INTERFACE -p tcp --dport $LOCAL_PORT -d $LOCAL_IP -j ACCEPT
}

# example: 192.168.10.100:80,ens18:8080;192.168.10.100.22,ens18:10022
NAT_RULES=${NAT_RULES:-""}
if [ -n "$NAT_RULES" ]; then
    IFS=';' read -r -a rules <<< "$NAT_RULES"
    for rule in "${rules[@]}"; do
        IFS=',' read -r local_info external_info <<< "$rule"
        IFS=':' read -r LOCAL_IP LOCAL_PORT <<< "$local_info"
        IFS=':' read -r WAN_INTERFACE EXTERNAL_PORT <<< "$external_info"
        setup_nat_port_forward "$WAN_INTERFACE" "$LOCAL_IP" "$LOCAL_PORT" "$EXTERNAL_PORT"
    done
fi
