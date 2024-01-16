# install k8s environment with podman rootless

## references

* https://kind.sigs.k8s.io/docs/user/rootless/
* https://kind.sigs.k8s.io/docs/user/configuration/

## prepare materials

1. [fedora 38](#) <!-- TODO installation doc in linux category -->
2. root account is not required(except ['configure for rootless'](#configure-for-rootless))
3. podman
    * ```shell
      sudo dnf -y install podman
      ```
4. kind binary
    * ```shell
      MIRROR="files.m.daocloud.io/"
      KIND_VERSION=v0.20.0
      [ $(uname -m) = x86_64 ] && curl -Lo kind "https://${MIRROR}github.com/kubernetes-sigs/kind/releases/download/${KIND_VERSION}/kind-linux-amd64"
      [ $(uname -m) = aarch64 ] && curl -Lo kind "https://${MIRROR}github.com/kubernetes-sigs/kind/releases/download/${KIND_VERSION}/kind-linux-arm64"
      chmod u+x kind
      mkdir -p $HOME/bin
      mv kind $HOME/bin
      ```
5. kubectl binary
    * ```shell
      MIRROR="files.m.daocloud.io/"
      VERSION=$(curl -L -s https://${MIRROR}dl.k8s.io/release/stable.txt)
      [ $(uname -m) = x86_64 ] && curl -LO "https://${MIRROR}dl.k8s.io/release/${VERSION}/bin/linux/amd64/kubectl"
      [ $(uname -m) = aarch64 ] && curl -LO "https://${MIRROR}dl.k8s.io/release/${VERSION}/bin/linux/arm64/kubectl"
      chmod u+x kubectl
      mkdir -p $HOME/bin
      mv kubectl $HOME/bin
      ```
6. image of kind node
    * ```shell
      MIRROR="m.daocloud.io/"
      IMAGE=docker.io/kindest/node:v1.29.0
      podman pull ${MIRROR}${IMAGE}
      podman tag ${MIRROR}${IMAGE} ${IMAGE}
      ```

## configure for rootless
1. The host needs to be running with cgroup v2
    * ```shell
      podman info | grep Cgroup
      ```
    * expected output contains `cgroupVersion: v2`
    * if not, see: https://kind.sigs.k8s.io/docs/user/rootless/#host-requirements
2. configure systemd
    * ```shell
      sudo mkdir -p /etc/systemd/system/user@.service.d
      sudo bash -c 'cat > /etc/systemd/system/user@.service.d/delegate.conf <<EOF
      [Service]
      Delegate=yes
      EOF'
      ```
3. configure modules of iptables
    * ```shell
      sudo bash -c 'cat > /etc/modules-load.d/iptables.conf <<EOF
      ip6_tables
      ip6table_nat
      ip_tables
      iptable_nat
      EOF'
      ```
4. reload
    * ```shell
      sudo systemctl daemon-reload
      sudo systemctl restart podman
      ```

## start/stop with default configuration

* ```shell
  KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --image=docker.io/kindest/node:v1.29.0
  # you can use kubectl to interact with the k8s cluster when succeed
  # kubectl get pod -A
  ```
* ```shell
  KIND_EXPERIMENTAL_PROVIDER=podman kind delete cluster
  ```

## start with custom configuration
1. prepare configuration file named `kind.yaml`
    * ```yaml
      <!-- @include: kind.yaml -->
      ```
    * NOTE: one control-plane with two workers
    * NOTE: container port 32080 and 32443 are bind to host ports(80 and 443)
2. start
    * ```shell
      KIND_EXPERIMENTAL_PROVIDER=podman kind create cluster --image=docker.io/kindest/node:v1.29.0k --config kind.yaml
      ```
