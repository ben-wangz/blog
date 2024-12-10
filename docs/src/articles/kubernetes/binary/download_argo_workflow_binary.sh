MIRROR="files.daocloud.io/"
VERSION=v3.5.4
curl -sSLo argo-linux-amd64.gz "https://${MIRROR}github.com/argoproj/argo-workflows/releases/download/${VERSION}/argo-linux-amd64.gz"
gunzip argo-linux-amd64.gz
chmod u+x argo-linux-amd64
mkdir -p ${HOME}/bin
mv -f argo-linux-amd64 ${HOME}/bin/argo
rm -f argo-linux-amd64.gz
