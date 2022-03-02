# nexus-repository-manager

## main usage

* a universal binary repository
* but we use it to provide maven package repository service only

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `nexus-repository-manager`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="nexus.repository.manager.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_sonatype_nexus3_3.37.3.dim" \
          "docker.io_gradle_7.4.0-jdk8.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install nexus-repository-manager
    * prepare [nexus.repository.manager.values.yaml](
      resources/nexus-repository-manager/nexus.repository.manager.values.yaml.md)
        + change password of admin by `secrets.htpasswd.password`
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/sonatype/nexus3:3.37.3"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-nexus-repository-manager \
              https://resource.geekcity.tech/kubernetes/charts/https/sonatype.github.io/helm3-charts/nexus-repository-manager-37.3.2.tgz \
              --values nexus.repository.manager.values.yaml \
              --atomic
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: nexus-repository-manager.local' https://localhost
      ```
2. works as a npm proxy and private registry that can publish packages
    * `commons-math3` not in storage before actions
        + ```shell
          kubectl -n application exec -it deployment/my-nexus-repository-manager -- \
              bash -c 'grep -r commons-math3 /nexus-data/blobs || echo not found anything'
          ```
    * prepare insecure(http) protocol for connection
        + prepare [insecure.nexus.repository.manager.ingress.yaml](
          resources/nexus-repository-manager/insecure.nexus.repository.manager.ingress.yaml.md)
        + apply to cluster
            * ```shell
              kubectl get namespace test > /dev/null 2>&1 || kubectl create namespace test \
                  && kubectl -n test apply -f insecure.nexus.repository.manager.ingress.yaml
              ```
    * check connection
        + ```shell
          curl --insecure --header 'Host: insecure.nexus.repository.manager.local' https://localhost
          ```
    * configure `maven-central` repository
        + configure hosts
            * ```shell
              echo $QEMU_HOST_IP nexus-repository-manager.local >> /etc/hosts
              ```
        + extract default admin password
            * ```shell
              kubectl -n application exec -it deployment/my-nexus-repository-manager -- cat /nexus-data/admin.password
              ```
        + visit `https://nexus-repository-manager.local/` and login with `username=admin` and extracted password
        + change `admin`'s password with random password generated
            * ```shell
              PASSWORD=($((echo -n $RANDOM | md5sum 2>/dev/null) || (echo -n $RANDOM | md5 2>/dev/null))) \
                  && echo $PASSWORD
              ```
        + enable `anonymous access`
        + visit `https://nexus-repository-manager.local/#admin/repository/repositories:maven-central`
          and change `Proxy`.`Remote storage` to `https://maven.aliyun.com/repository/central` then click `Save` button
    * prepare [nexus.repository.manager.test.sh](resources/nexus-repository-manager/nexus.repository.manager.test.sh.md)
    * prepare [build.gradle](resources/nexus-repository-manager/build.gradle.md)
    * run npm install
        + ```shell
          docker run --rm \
              --add-host insecure.nexus.repository.manager.local:172.17.0.1 \
              -e ADMIN_PASSWORD=$PASSWORD \
              -v $(pwd)/nexus.repository.manager.test.sh:/app/nexus.repository.manager.test.sh:ro \
              -v $(pwd)/build.gradle:/app/build.gradle:ro \
              --workdir /app \
              -it docker.io/gradle:7.4.0-jdk8 \
              bash '/app/nexus.repository.manager.test.sh'
          ```
    * `commons-math3` in storage after actions
        + ```shell
          kubectl -n application exec -it deployment/my-nexus-repository-manager -- \
              grep -r commons-math3 /nexus-data/blobs
          ```
    * visit with web browser and check package published
        + visit: `https://nexus-repository-manager.local/#browse/browse:maven-releases`

## uninstallation

1. uninstall `nexus-repository-manager`
    * ```shell
      helm -n application uninstall my-nexus-repository-manager
      # NOTE: pvc will be deleted automatically
      #kubectl -n application delete pvc my-nexus-repository-manager-data
      ```