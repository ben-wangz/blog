# kubernetes dashboard

## main usage

* a web based dashboard to manage kubernetes cluster

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* setup kubernetes dashboard
* create a read only user

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="dashboard.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_kubernetesui_dashboard_v2.4.0.dim" \
          "docker.io_kubernetesui_metrics-scraper_v1.0.7.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install dashboard
    * prepare [dashboard.values.yaml](resources/dashboard/dashboard.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/kubernetesui/dashboard:v2.4.0" \
              "docker.io/kubernetesui/metrics-scraper:v1.0.7"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-dashboard \
              https://resource.geekcity.tech/kubernetes/charts/https/kubernetes.github.io/dashboard/kubernetes-dashboard-5.0.5.tgz \
              --values dashboard.values.yaml \
              --atomic
          ```
    * wait for all pods to be ready
        + ```shell
          kubectl -n application wait --for=condition=ready pod --all
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: dashboard.local' https://localhost
      ```
2. create read only `user`
    * prepare [create.user.yaml](resources/dashboard/create.user.yaml.md)
    * ```shell
      kubectl apply -f create.user.yaml
      ```
3. extract user token
    * ```shell
      kubectl -n application get secret $( \
          kubectl -n application get ServiceAccount dashboard-ro -o jsonpath="{.secrets[0].name}" \
      ) -o jsonpath="{.data.token}" \
          | base64 --decode \
          && echo
      ```
4. visit via website
    * add hosts info
        + ```shell
          echo "$IP dashboard.local" >> /etc/hosts
          ```
    * visit `https://dashboard.local`
    * use the extracted token to login

## uninstallation

1. delete rbac resources
    * ```shell
      kubectl delete -f create.user.yaml
      ```
2. uninstall `dashboard`
    * ```shell
      helm -n application uninstall my-dashboard
      ```