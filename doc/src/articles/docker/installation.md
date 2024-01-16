# docker installation

## podman is recommended to replace docker
* [installation](https://podman.io/docs/installation)
* installation is simple for fedora
    + ```shell
      sudo dnf install podman
      ```

## install with CentOS 8

* ```shell
  cat > /etc/yum.repos.d/docker-ce.repo <<EOF
  [docker-ce-stable]
  name=Docker CE Stable - $basearch
  baseurl=https://mirrors.aliyun.com/docker-ce/linux/centos/\$releasever/\$basearch/stable
  enabled=1
  gpgcheck=1
  gpgkey=https://mirrors.aliyun.com/docker-ce/linux/centos/gpg
  EOF
  dnf install -y yum-utils device-mapper-persistent-data lvm2 docker-ce \
      && systemctl enable docker \
      && systemctl start docker
  ```

## install with Ubuntu 20.04.2 LTS (x86_64)

* ```shell
  apt-get install apt-transport-https ca-certificates curl gnupg lsb-release
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
  echo \
      "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
      | tee /etc/apt/sources.list.d/docker.list > /dev/null
  apt-get install docker-ce docker-ce-cli containerd.io
  ```
* reference to https://docs.docker.com/engine/install/ubuntu/ for other arch

## install docker Desktop with mac/windows

* reference to https://www.docker.com/products/docker-desktop for mac
* reference to https://docs.docker.com/desktop/windows/install/ for windows
