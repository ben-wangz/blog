#REGISTRY_INSECURE=false
#REGISTRY=REGISTRY
#REGISTRY_USERNAME=your-username
#REGISTRY_PASSWORD=your-password
kubectl -n business-workflows create secret generic registry-credentials \
  --from-literal="insecure=${REGISTRY_INSECURE:-false}" \
  --from-literal="registry=${REGISTRY:-docker.io}" \
  --from-literal="username=${REGISTRY_USERNAME:-wangz2019}" \
  --from-literal="password=${REGISTRY_PASSWORD}"
