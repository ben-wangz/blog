MIRROR="files."
VERSION=v2.9.3
[ $(uname -m) = x86_64 ] && curl -sSLo argocd "https://${MIRROR}github.com/argoproj/argo-cd/releases/download/${VERSION}/argocd-linux-amd64"
[ $(uname -m) = aarch64 ] && curl -sSLo argocd "https://${MIRROR}github.com/argoproj/argo-cd/releases/download/${VERSION}/argocd-linux-arm64"
chmod u+x argocd
mkdir -p ${HOME}/bin
mv -f argocd ${HOME}/bin
