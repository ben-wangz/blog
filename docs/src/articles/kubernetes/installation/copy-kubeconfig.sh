# can be run by any user with sudo privilege
mkdir -p $HOME/.kube \
    && sudo cp /etc/kubernetes/admin.conf $HOME/.kube/config \
    && sudo chown $UID:$UID $HOME/.kube/config