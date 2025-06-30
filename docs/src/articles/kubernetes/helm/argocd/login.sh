#! /bin/bash

PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 --decode)
FIRST_MASTER_IP=$(kubectl get nodes --selector=node-role.kubernetes.io/control-plane \
    -o jsonpath='{$.items[0].status.addresses[?(@.type=="InternalIP")].address}') \
    && argocd login --insecure --username admin $FIRST_MASTER_IP:30443 --password $PASSWORD
