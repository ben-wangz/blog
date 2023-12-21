# prepare offline resource for kubespray installation

## overview
* [prepare offline resource for kubespray installation](#prepare-offline-resource-for-kubespray-installation)
    * [overview](#overview)
    * [container runtime](#container-runtime)
    * [offline resource directory](#offline-resource-directory)
    * [build inventory for kubespray](#build-inventory-for-kubespray)
    * [download files and images](#download-files-and-images)
    * [start nginx to serve files](#start-nginx-to-serve-files)
    * [start registry to serve images](#start-registry-to-serve-images)
    * [create local yum repository](#create-local-yum-repository)

## container runtime
1. install podman
    * ```shell
      sudo dnf -y install podman
      ```
2. specify container runtime(recommended)
    * ```shell
      export CONTAINER_RUNTIME=podman
      ```
3. verify/find container runtime automatically
    * ```shell
      function find_container_runtime(){
          if command -v nerdctl 1>/dev/null 2>&1; then
              echo "nerdctl"
          elif command -v podman 1>/dev/null 2>&1; then
              echo "podman"
          elif command -v docker 1>/dev/null 2>&1; then
              echo "docker"
          else
              echo "No supported container runtime found"
              exit 1
          fi
      }
      if [ -z "$CONTAINER_RUNTIME" ]; then
          CONTAINER_RUNTIME=$(find_container_runtime)
          echo "CONTAINER_RUNTIME=$CONTAINER_RUNTIME found"
      else
          if command -v $CONTAINER_RUNTIME 1>/dev/null 2>&1; then
              echo "CONTAINER_RUNTIME=$CONTAINER_RUNTIME verified"
          else
              echo "CONTAINER_RUNTIME=$CONTAINER_RUNTIME but not exits"
          fi
      fi
      ```

## turn off selinux
1. turn off selinux permanently
    * ```shell
      sudo sed -i 's/^SELINUX=.*/SELINUX=disabled/g' /etc/selinux/config
      sudo setenforce 0
      ```

## offline resource directory
1. specify offline resource directory
    * ```shell
      export OFFLINE_RESOURCE_DIRECTORY=$HOME/kubespray-offline-resource
      ```
2. create offline resource directory
    * ```shell
      mkdir -p $OFFLINE_RESOURCE_DIRECTORY
      ```

## build inventory for kubespray
1. specify kubespary image
    * ```shell
      export KUBESPRAY_IMAGE=quay.io/kubespray/kubespray:v2.23.1
      ```
2. copy sample inventory
    * ```shell
      INVENTORY_DIRECTORY=$OFFLINE_RESOURCE_DIRECTORY/inventory
      mkdir -p $INVENTORY_DIRECTORY
      $CONTAINER_RUNTIME run --rm \
          -v $INVENTORY_DIRECTORY:/my-inventory \
          -it $KUBESPRAY_IMAGE \
          /bin/cp -rfp /kubespray/inventory/sample /my-inventory/single-master
      ```
3. create inventory file
    * ```shell
      INVENTORY_FILE=$INVENTORY_DIRECTORY/single-master/hosts.yaml
      declare -a IPS=(192.168.123.107)
      $CONTAINER_RUNTIME run --rm \
          -v $INVENTORY_DIRECTORY:/my-inventory \
          -e CONFIG_FILE=/my-inventory/single-master/hosts.yaml \
          -it $KUBESPRAY_IMAGE \
          python3 /kubespray/contrib/inventory_builder/inventory.py ${IPS[@]}
      ```

## download files and images
1. generate offline files and image list
    * ```shell
      $CONTAINER_RUNTIME run --rm \
          -v $INVENTORY_DIRECTORY:/my-inventory \
          -it $KUBESPRAY_IMAGE \
          bash -c '/kubespray/contrib/offline/generate_list.sh -i /my-inventory/single-master/hosts.yaml \
              && /bin/cp /kubespray/contrib/offline/temp/files.list /my-inventory/single-master/files.list \
              && /bin/cp /kubespray/contrib/offline/temp/images.list /my-inventory/single-master/images.list'
      ```
2. modify files.list as aria2 input file
    * ```shell
      cat << 'EOF' > $INVENTORY_DIRECTORY/aria2_input_file_transform.py
      import os
      from urllib.parse import urlparse
      
      download_path = os.environ.get("DOWNLOAD_PATH", "/tmp/download")
      input_file_path = os.environ.get("INPUT_FILE_PATH", "files.list")
      output_file_path = os.environ.get("OUTPUT_FILE_PATH", "files.list.aria2")
      with open(output_file_path, "w") as output_file:
          with open(input_file_path) as input_file:
              for line in input_file:
                  url = line.strip()
                  url_parsed = urlparse(url)
                  filename = os.path.basename(url_parsed.path)
                  path = os.path.dirname(url_parsed.path)
                  output_file.write(url + "\n")
                  output_file.write(" dir=" + os.path.join(download_path, path.strip("/")) + "\n")
                  output_file.write(" out=" + filename + "\n")
      EOF
      ```
    * ```shell
      DOWNLOAD_FILES_DIRECTORY=$INVENTORY_DIRECTORY/single-master/files
      mkdir -p $DOWNLOAD_FILES_DIRECTORY
      FILES_LIST=$INVENTORY_DIRECTORY/single-master/files.list
      FILES_LIST_ARIA2=$DOWNLOAD_FILES_DIRECTORY/files.list.aria2
      $CONTAINER_RUNTIME run --rm \
          -v $DOWNLOAD_FILES_DIRECTORY:/download/files \
          -v $INVENTORY_DIRECTORY/single-master/files.list:/download/files.list:ro \
          -v $INVENTORY_DIRECTORY/aria2_input_file_transform.py:/tmp/aria2_input_file_transform.py:ro \
          -e DOWNLOAD_PATH=/download/files \
          -e INPUT_FILE_PATH=/download/files.list \
          -e OUTPUT_FILE_PATH=/download/files/files.list.aria2 \
          -it docker.io/library/python:3.12.1-alpine3.19 \
          python3 /tmp/aria2_input_file_transform.py
      ```
2. download files
    * ```shell
      $CONTAINER_RUNTIME run --rm \
          -v $DOWNLOAD_FILES_DIRECTORY:/download/files \
          -v $FILES_LIST_ARIA2:/download/files.list.aria2:ro \
          -it docker.io/library/alpine:3.19.0 sh -c 'apk add aria2 && aria2c -i /download/files.list.aria2 --max-tries=0 --continue'
      ```
3. download images
    * ```shell
      DOWNLOAD_IMAGES_DIRECTORY=$INVENTORY_DIRECTORY/single-master/images
      mkdir -p $DOWNLOAD_IMAGES_DIRECTORY
      while read IMAGE
      do
          FILE_NAME="$(echo $IMAGE | sed s@"/"@"-"@g | sed s/":"/"-"/g)".dim
          IMAGE_FILE=$DOWNLOAD_IMAGES_DIRECTORY/$FILE_NAME
          $CONTAINER_RUNTIME image inspect $IMAGE > /dev/null || $CONTAINER_RUNTIME pull $IMAGE
          $CONTAINER_RUNTIME save -o $IMAGE_FILE $IMAGE
      done < $INVENTORY_DIRECTORY/single-master/images.list
      ```

## start nginx to serve files
1. prepare nginx.conf
    * ```shell
      cat << 'EOF' > $INVENTORY_DIRECTORY/single-master/nginx.conf
      user nginx;
      worker_processes auto;
      error_log /var/log/nginx/error.log;
      pid /run/nginx.pid;
      include /usr/share/nginx/modules/*.conf;
      events {
          worker_connections 1024;
      }
      http {
          log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                            '$status $body_bytes_sent "$http_referer" '
                            '"$http_user_agent" "$http_x_forwarded_for"';
          access_log  /var/log/nginx/access.log  main;
          sendfile            on;
          tcp_nopush          on;
          tcp_nodelay         on;
          keepalive_timeout   65;
          types_hash_max_size 2048;
          default_type        application/octet-stream;
          include /etc/nginx/conf.d/*.conf;
          server {
              listen       80 default_server;
              listen       [::]:80 default_server;
              server_name  _;
              include /etc/nginx/default.d/*.conf;
              location / {
                  root    /usr/share/nginx/html/download;
              autoindex on;
              autoindex_exact_size off;
              autoindex_localtime on;
              }
              error_page 404 /404.html;
                  location = /40x.html {
              }
              error_page 500 502 503 504 /50x.html;
                  location = /50x.html {
              }
          }
      }
      EOF
      ```
2. start nginx container
    * ```shell
      $CONTAINER_RUNTIME run --restart always \
          -v $INVENTORY_DIRECTORY/single-master/nginx.conf:/etc/nginx/nginx.conf:ro \
          -v $DOWNLOAD_FILES_DIRECTORY:/usr/share/nginx/html/download:ro \
          -p 8080:80 \
          -d docker.io/library/nginx:1.19.7-alpine
      ```

## start registry to serve images
1. generate ssl
    * ```shell
      IP_ADDRESS=192.168.1.107
      REGISTRY_DIRECTORY=$INVENTORY_DIRECTORY/single-master/registry
      mkdir -p $REGISTRY_DIRECTORY
      cat << EOF > $REGISTRY_DIRECTORY/san.conf
      [req]
      default_bits = 4096
      default_md = sha256
      distinguished_name = req_distinguished_name
      x509_extensions = v3_req
      prompt = no
      [req_distinguished_name]
      C = CN
      ST = ZheJiang
      L = HangZhou
      O = ZhejiangLab
      OU = astronomy
      CN = $IP_ADDRESS
      [v3_req]
      keyUsage = keyEncipherment, dataEncipherment
      extendedKeyUsage = serverAuth
      subjectAltName = @alt_names
      [alt_names]
      IP.1 = $IP_ADDRESS
      EOF
      ```
    * ```shell
      mkdir -p $REGISTRY_DIRECTORY/certs
      SSL_KEY_FILE=$REGISTRY_DIRECTORY/certs/domain.key
      openssl genrsa 4096 > $SSL_KEY_FILE
      chmod 400 $REGISTRY_DIRECTORY/certs/domain.key
      openssl req -new -x509 -nodes -sha256 -days 365 -key $SSL_KEY_FILE -out $REGISTRY_DIRECTORY/certs/domain.crt --config $REGISTRY_DIRECTORY/san.conf
      ```
2. start local registry
    * ```shell
      mkdir -p $REGISTRY_DIRECTORY/storage
      $CONTAINER_RUNTIME run --restart always \
          --name local-registry \
          -v $REGISTRY_DIRECTORY/certs:/certs \
          -v $REGISTRY_DIRECTORY/storage:/var/lib/registry \
          -e REGISTRY_HTTP_ADDR=0.0.0.0:5443 \
          -e REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt \
          -e REGISTRY_HTTP_TLS_KEY=/certs/domain.key \
          -p 5443:5443 \
          -p 5000:5000 \
          -d registry:2.7.1
      ```
3. load images and push them to local registry
    * ```shell
      # there's fundamentally no difference between CER and CRT files
      ln -s $REGISTRY_DIRECTORY/certs/domain.crt $REGISTRY_DIRECTORY/certs/domain.cert
      while read IMAGE
      do
          FILE_NAME="$(echo $IMAGE | sed s@"/"@"-"@g | sed s/":"/"-"/g)".dim
          IMAGE_FILE=$DOWNLOAD_IMAGES_DIRECTORY/$FILE_NAME
          $CONTAINER_RUNTIME image inspect $IMAGE > /dev/null || $CONTAINER_RUNTIME load -i $IMAGE_FILE
          IMAGE_ID=$($CONTAINER_RUNTIME image inspect --format '{{.Id}}' $IMAGE)
          $CONTAINER_RUNTIME tag $IMAGE_ID $IP_ADDRESS:5443/$IMAGE
          if [ "podman" == "$CONTAINER_RUNTIME" ]; then
              CERT_DIR_OPTION="--cert-dir=$REGISTRY_DIRECTORY/certs"
          else
              CERT_DIR_OPTION=""
              mkdir -p /etc/docker/certs.d
              sudo cp -r $REGISTRY_DIRECTORY/certs /etc/docker/certs.d/$IP_ADDRESS:5443
          fi
          $CONTAINER_RUNTIME push $CERT_DIR_OPTION $IP_ADDRESS:5443/$IMAGE
      done < $INVENTORY_DIRECTORY/single-master/images.list
      ```
3. the storage(`$REGISTRY_DIRECTORY/storage`) can be migrated any where with the docker registry

## create local yum repository
1. create YUM_REPO_CONF
    * ```shell
      YUM_REPO_CONF_DIRECTORY=$HOME/kubespray-offline-resource/fedora-38
      cat << 'EOF' > $YUM_REPO_CONF_DIRECTORY/fedora.repo
      [fedora]
      name=Fedora $releasever - $basearch
      #baseurl=http://download.example/pub/fedora/linux/releases/$releasever/Everything/$basearch/os/
      metalink=https://mirrors.fedoraproject.org/metalink?repo=fedora-$releasever&arch=$basearch
      enabled=1
      countme=1
      metadata_expire=7d
      repo_gpgcheck=0
      type=rpm
      gpgcheck=1
      gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-fedora-$releasever-$basearch
      skip_if_unavailable=False
      ```
1. create local yum repository
    * ```shell
      YUM_REPOSITORY_DIRECTORY=$HOME/kubespray-offline-resource/yum-repository
      $CONTAINER_RUNTIME run --rm \
          -v $YUM_REPOSITORY_DIRECTORY:/yum-repository \
          -v $YUM_REPO_CONF_DIRECTORY:/etc/yum.repos.d:ro \
          -it fedora:38 \
          bash -c 'dnf install -y createrepo && createrepo /yum-repository'
      ```