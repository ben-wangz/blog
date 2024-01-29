curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
mv minikube-linux-amd64 minikube
chmod u+x minikube
mkdir -p ${HOME}/bin
mv -f minikube ${HOME}/bin