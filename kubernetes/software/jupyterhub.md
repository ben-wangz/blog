# jupyterhub

## main usage

* jupyterhub

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `jupyterhub`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="jupyterhub.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
              "docker.io_jupyterhub_k8s-hub_1.2.0.dim" \
              "docker.io_jupyterhub_configurable-http-proxy_4.5.0.dim" \
              "docker.io_traefik_v2.4.11.dim" \
              "docker.io_jupyterhub_k8s-secret-sync_1.2.0.dim" \
              "docker.io_jupyterhub_k8s-singleuser-sample_1.2.0.dim" \
              "docker.io_k8s.gcr.io_kube-scheduler_v1.19.13.dim" \
              "docker.io_k8s.gcr.io_pause_3.5.dim" \
              "docker.io_jupyterhub_k8s-image-awaiter_1.2.0.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace application > /dev/null 2>&1 || kubectl create namespace application \
              && kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
4. install jupyterhub
    * prepare [jupyterhub.values.yaml](resources/jupyterhub/jupyterhub.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/jupyterhub/k8s-hub:1.2.0" \
              "docker.io/jupyterhub/configurable-http-proxy:4.5.0" \
              "docker.io/traefik:v2.4.11" \
              "docker.io/jupyterhub/k8s-secret-sync:1.2.0" \
              "docker.io/jupyterhub/k8s-singleuser-sample:1.2.0" \
              "docker.io/k8s.gcr.io/kube-scheduler:v1.19.13" \
              "docker.io/k8s.gcr.io/pause:3.5" \
              "docker.io/jupyterhub/k8s-image-awaiter:1.2.0"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-jupyterhub \
              https://resource.geekcity.tech/kubernetes/charts/https/jupyterhub.github.io/helm-chart/jupyterhub-1.2.0.tgz \
              --values jupyterhub.values.yaml \
              --atomic
          ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: jupyterhub.local' https://localhost/hub/login
      ```
2. visit gitea via website
    * configure hosts
        + ```shell
          echo $QEMU_HOST_IP jupyterhub.local >> /etc/hosts
          ```
    * visit `https://jupyterhub.local:10443/` with your browser
    * login with
        + default user: `admin`
        + password: `a-shared-secret-password`

## uninstallation

1. uninstall `jupyterhub`
    * ```shell
      helm -n application uninstall my-jupyterhub
      # delete pvc created
      #helm -n application get pvc
      #helm -n application delete pvc ...
      ```

## more configurations and manual
1. https://zero-to-jupyterhub.readthedocs.io/en/latest/resources/reference.html
2. https://zero-to-jupyterhub.readthedocs.io/en/latest/jupyterhub/customization.html
3. https://jupyterhub.readthedocs.io/en/stable/reference/rest-api.html