#! /bin/bash

set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

bash "$SCRIPT_DIR/ip-forward.router.sh"
bash "$SCRIPT_DIR/dnsmasq/dnsmasq.sh"
bash "$SCRIPT_DIR/ip-forward.nat.sh"
