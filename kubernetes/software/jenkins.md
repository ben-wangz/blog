# jenkins

## main usage

* The leading open source automation server, Jenkins provides hundreds of plugins to support building, deploying and
  automating any project

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `jenkins`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="jenkins.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_jenkins_jenkins_2.319.3-jdk11.dim" \
          "docker.io_kiwigrid_k8s-sidecar_1.15.0.dim" \
          "docker.io_jenkins_inbound-agent_4.11.2-4.dim" \
          "docker.io_maorfr_kube-tasks_0.2.0.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install jenkins
    * prepare [jenkins.values.yaml](resources/jenkins/jenkins.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/jenkins/jenkins:2.319.3-jdk11" \
              "docker.io/kiwigrid/k8s-sidecar:1.15.0" \
              "docker.io/jenkins/inbound-agent:4.11.2-4" \
              "docker.io/maorfr/kube-tasks:0.2.0"
          ```
    * create `jenkins-admin-secret`
        + ```shell
          # uses the "Array" declaration
          # referencing the variable again with as $PASSWORD an index array is the same as ${PASSWORD[0]}
          PASSWORD=($((echo -n $RANDOM | md5sum 2>/dev/null) || (echo -n $RANDOM | md5 2>/dev/null)))
          kubectl -n application \
              create secret generic jenkins-admin-secret \
              --from-literal=username=admin \
              --from-literal=password=$PASSWORD
          ```
    * install by helm
        + ```shell
          # NOTE: jenkins will download plugins from remote for a long time
          helm install \
              --create-namespace --namespace application \
              my-jenkins \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.jenkins.io/jenkins-3.11.4.tgz \
              --values jenkins.values.yaml \
              --atomic \
              --timeout 10m
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: jenkins.local' https://localhost
      ```
2. visit jenkins via website
    * configure hosts
        + ```shell
          echo $QEMU_HOST_IP jenkins.local >> /etc/hosts
          ```
    * visit `https://jenkins.local:10443/` with your browser
        + extract username and password of admin user
            * ```shell
              kubectl -n application get secret jenkins-admin-secret -o jsonpath="{.data.username}" | base64 --decode && echo
              kubectl -n application get secret jenkins-admin-secret -o jsonpath="{.data.password}" | base64 --decode && echo
              ```
        + login with username and password extracted

## uninstallation

1. uninstall `jenkins`
    * ```shell
      helm -n application uninstall my-jenkins
      # NOTE: pvc will be deleted automatically
      #kubectl -n application delete pvc my-jenkins
      ```