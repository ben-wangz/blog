# verdaccio

## main usage

* a lightweight private Node.js proxy registry.

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `verdaccio`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="verdaccio.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_verdaccio_verdaccio_5.2.0.dim" \
          "docker.io_node_17.5.0-alpine3.15.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install verdaccio
    * prepare [verdaccio.values.yaml](resources/verdaccio/verdaccio.values.yaml.md)
        + change password of admin by `secrets.htpasswd.password`
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/verdaccio/verdaccio:5.2.0"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-verdaccio \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.verdaccio.org/verdaccio-4.6.2.tgz \
              --values verdaccio.values.yaml \
              --atomic
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: verdaccio.local' https://localhost
      ```
2. works as a npm proxy and private registry that can publish packages
    * nothing in storage before actions
        + ```shell
          kubectl -n application exec -it  deployment/my-verdaccio -- ls -l /verdaccio/storage/data
          ```
    * prepare [npm.registry.test.sh](resources/verdaccio/npm.registry.test.sh.md)
    * prepare [npm.login.expect](resources/verdaccio/npm.login.expect.md)
    * run npm install
        + ```shell
          docker run --rm \
              --add-host verdaccio.local:172.17.0.1 \
              -e NPM_ADMIN_USERNAME=admin \
              -e NPM_ADMIN_PASSWORD=your-admin-password \
              -e NPM_LOGIN_EMAIL=your-email@some.domain \
              -e NPM_REGISTRY=https://verdaccio.local \
              -v $(pwd)/npm.registry.test.sh:/app/npm.registry.test.sh:ro \
              -v $(pwd)/npm.login.expect:/app/npm.login.expect:ro \
              --workdir /app \
              -it docker.io/node:17.5.0-alpine3.15 \
              sh /app/npm.registry.test.sh
          ```
    * dependency packages in storage after actions
        + ```shell
          kubectl -n application exec -it deployment/my-verdaccio -- ls -l /verdaccio/storage/data
          ```
    * visit with web browser and check package published
        + configure hosts
            * ```shell
              echo $QEMU_HOST_IP verdaccio.local >> /etc/hosts
              ```
        + visit: `https://verdaccio.local`

## uninstallation

1. uninstall `verdaccio`
    * ```shell
      helm -n application uninstall my-verdaccio
      # NOTE: pvc will be deleted automatically
      #kubectl -n application delete pvc my-verdaccio
      ```