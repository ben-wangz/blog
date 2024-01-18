swapoff -a
sed -i -Ee 's/^([^#].+ swap[ \t].*)/#\1/' /etc/fstab