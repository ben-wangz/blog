# gitea

## main usage

* service for git repositories

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `gitea`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="gitea.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "gitea_gitea_1.15.3.dim" \
          "docker.io_bitnami_memcached_1.6.9-debian-10-r114.dim" \
          "docker.io_bitnami_memcached-exporter_0.8.0-debian-10-r105.dim" \
          "docker.io_bitnami_postgresql_11.11.0-debian-10-r62.dim" \
          "docker.io_bitnami_bitnami-shell_10.dim" \
          "docker.io_bitnami_postgres-exporter_0.9.0-debian-10-r34.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install gitea
    * prepare [gitea.values.yaml](resources/gitea/gitea.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/gitea/gitea:1.15.3" \
              "docker.io/bitnami/memcached:1.6.9-debian-10-r114" \
              "docker.io/bitnami/memcached-exporter:0.8.0-debian-10-r105" \
              "docker.io/bitnami/postgresql:11.11.0-debian-10-r62" \
              "docker.io/bitnami/bitnami-shell:10" \
              "docker.io/bitnami/postgres-exporter:0.9.0-debian-10-r34"
          ```
    * create `gitea-admin-secret`
        + ```shell
          # uses the "Array" declaration
          # referencing the variable again with as $PASSWORD an index array is the same as ${PASSWORD[0]}
          PASSWORD=($((echo -n $RANDOM | md5sum 2>/dev/null) || (echo -n $RANDOM | md5 2>/dev/null)))
          # NOTE: username should have at least 6 characters
          kubectl -n application \
              create secret generic gitea-admin-secret \
              --from-literal=username=gitea_admin \
              --from-literal=password=$PASSWORD
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-gitea \
              https://resource.geekcity.tech/kubernetes/charts/https/dl.gitea.io/charts/gitea-4.1.1.tgz \
              --values gitea.values.yaml \
              --atomic \
              --set gitea.config.mailer.PASSWD=$YOUR_MAILER_PASSWORD
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: gitea.local' https://localhost
      ```
2. visit gitea via website
    * configure hosts
        + ```shell
          echo $QEMU_HOST_IP gitea.local >> /etc/hosts
          ```
    * visit `https://gitea.local:10443/` with your browser
    * extract username and password of admin user
        + ```shell
          kubectl -n application get secret gitea-admin-secret -o jsonpath="{.data.username}" | base64 --decode && echo
          kubectl -n application get secret gitea-admin-secret -o jsonpath="{.data.password}" | base64 --decode && echo
          ```
    * login as admin user
    * create repository named `test-repo`
    * add ssh public key
        + ```shell
          # in QEMU machine
          ssh-keygen -t rsa -b 4096 -N "" -f ~/.ssh/id_rsa
          ```
        + in browser: `https://gitea.local:10443/user/settings/keys`
            * find "Manage SSH Keys"
            * click "Add Key"
            * paste content of file `~/.ssh/id_rsa.pub`
            * click "Add Key"
3. visit gitea via ssh
    * ```shell
      # in QEMU machine
      dnf -y install git
      echo 127.0.0.1 gitea.local >> /etc/hosts
      git config --global user.email "you@example.com"
      git config --global user.name "Your Name"
      
      mkdir -p test-repo && cd test-repo
      
      touch README.md
      git init
      git checkout -b main
      git add README.md
      git commit -m "first commit"
      git remote add origin ssh://git@gitea.local:1022/gitea_admin/test-repo.git
      git push -u origin main
      ```
4. test email feature by creating a user and sending notification email to the user

## uninstallation

1. uninstall `gitea`
    * ```shell
      helm -n application uninstall my-gitea \
          && kubectl -n application delete pvc data-my-gitea-0 data-my-gitea-postgresql-0 \
          && kubectl -n application delete secret gitea-admin-secret
      ```