systemctl stop swap-create@zram0
dnf remove -y zram-generator-defaults
swapoff -a