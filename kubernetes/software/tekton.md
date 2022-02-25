# tekton

## main usage

* a cloud-native solution for building CI/CD systems
* Tekton Pipelines, which provides the building blocks, and of supporting components, such as Tekton CLI and Tekton
  Catalog, that make Tekton a complete ecosystem.

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `tekton`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="tekton.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_gcr.io_tekton-releases_github.com_tektoncd_operator_cmd_kubernetes_proxy-webhook_v0.54.0.dim" \
          "docker.io_gcr.io_tekton-releases_dogfooding_tkn_latest-025de2.dim" \
          "docker.io_gcr.io_tekton-releases_github.com_tektoncd_operator_cmd_kubernetes_operator_v0.54.0.dim" \
          "docker.io_gcr.io_tekton-releases_github.com_tektoncd_operator_cmd_kubernetes_webhook_v0.54.0.dim" \
          "docker.io_busybox_1.33.1-uclibc.dim"
      ```
3. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl get namespace tekton-operator > /dev/null 2>&1 || kubectl create namespace tekton-operator \
              && kubectl -n tekton-operator apply -f self.signed.and.ca.issuer.yaml
          ```
4. prepare images
    * NOTE: `tekton-operator-v0.54.0` not support custom registry for components according
      to [issue #625](https://github.com/tektoncd/operator/issues/625)
    * run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
    * ```shell
      load_image "docker.registry.local:443" \
          "docker.io/gcr.io/tekton-releases/github.com/tektoncd/operator/cmd/kubernetes/proxy-webhook:v0.54.0" \
          "docker.io/gcr.io/tekton-releases/dogfooding/tkn:latest-025de2" \
          "docker.io/gcr.io/tekton-releases/github.com/tektoncd/operator/cmd/kubernetes/operator:v0.54.0" \
          "docker.io/gcr.io/tekton-releases/github.com/tektoncd/operator/cmd/kubernetes/webhook:v0.54.0"
      ```
    * load images at every node in cluster
        + ```shell
          DOCKER_IMAGE_PATH=/root/docker-images/tekton.software/all-nodes && mkdir -p $DOCKER_IMAGE_PATH
          BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64/tekton.software/all-nodes"
          for IMAGE in "docker.io_gcr.io_tekton-releases_github.com_tektoncd_dashboard_cmd_dashboard_v0.23.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_pullrequest-init_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_controller_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_webhook_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_entrypoint_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_triggers_cmd_controller_v0.18.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_git-init_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_triggers_cmd_eventlistenersink_v0.18.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_imagedigestexporter_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_triggers_cmd_interceptors_v0.18.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_kubeconfigwriter_v0.32.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_triggers_cmd_webhook_v0.18.0.dim" \
              "docker.io_gcr.io_tekton-releases_github.com_tektoncd_pipeline_cmd_nop_v0.32.0.dim"
          do
              IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
              if [ ! -f $IMAGE_FILE ]; then
                  TMP_FILE=$IMAGE_FILE.tmp \
                      && curl -o "$TMP_FILE" -L "$BASE_URL/$IMAGE" \
                      && mv $TMP_FILE $IMAGE_FILE
              fi
              docker image load -i $IMAGE_FILE
          done
          ```
    * load them into all nodes of kind cluster if kind environment specified
        + ```shell
          for IMAGE in "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/entrypoint:v0.32.0@sha256:7f50901900925357460e6c6c985580f0b69c0d316ade75965228adb8b081614e" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/dashboard/cmd/dashboard:v0.23.0@sha256:4f70cd5f10bb6c8594b7810cf1fd8a8950d535ef0bb95e2c5f214a620646d720" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/kubeconfigwriter:v0.32.0@sha256:32fec74288f52ede279f091d8bac91d48ff6538fa3290251339b0075c59d0947" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/git-init:v0.32.0@sha256:fe3310b87b9fad4b5139ac93f0e570c25fb97dcb64a876a5b8eebbc877fc12e8" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/nop:v0.32.0@sha256:a8ffddd75b7a7078d5c07d09259d7c5db04614b4c5ba5c43e99b0632034f2479" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/imagedigestexporter:v0.32.0@sha256:2b39f19517523df8a00a366a0d3adb815ca2623fc9c51f05dd290773d5d308c7" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/pullrequest-init:v0.32.0@sha256:632b5086dba4d7f30f5b1e77f9e5e551b06c9e897cf2afc93e100b26f9c32e39" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/controller:v0.32.0@sha256:0e4f92e95c9ae8140ddfc8751bb54cf54e1b00d27aa542c11d5ad8663c5067ae" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/pipeline/cmd/webhook:v0.32.0@sha256:f0e31a5b1218bef6ad6323c05b4ed54412555accf542ac8a9dd0221629f33189" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/eventlistenersink:v0.18.0@sha256:9453f8184a476433f9223172f75790efeedb0780172ba9bcaa564d6987d85c2b" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/controller:v0.18.0@sha256:c9bac56feb04c16a1b483a7fe50a723022c0f1dfe920d6704ca7566de8d473cf" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/interceptors:v0.18.0@sha256:ca8025d2471deb7f51826227b89634413c465c66e785565c8e4db02b8f2c00e9" \
              "docker.io/gcr.io/tekton-releases/github.com/tektoncd/triggers/cmd/webhook:v0.18.0@sha256:ccd1613eb4b64ff732e092619e9fb4594aa617d2b93dbd46ff091be394bfb0d7"
          do
              kind load docker-image $IMAGE
          done
          ```
5. install `tekton-operator`
    * prepare [tekton_operator_v0.54.0_release.yaml](resources/tekton/tekton_operator_v0.54.0_release.yaml.md)
    * install
        + ```shell
          kubectl -n tekton-operator apply -f tekton_operator_v0.54.0_release.yaml
          ```
    * wait for pods to be ready
        + ```shell
          kubectl -n tekton-operator wait --for=condition=ready pod --all
          ```
6. install components of `tekton`
    * prepare [tekton.config.yaml](resources/tekton/tekton.config.yaml.md)
    * install
        + ```shell
          kubectl -n tekton-operator apply -f tekton.config.yaml
          ```
    * prepare [tekton.ingress.yaml](resources/tekton/tekton.ingress.yaml.md)
    * apply ingress for `tekton-dashboard`
        + ```shell
          kubectl -n tekton-pipelines apply -f tekton.ingress.yaml
          ```
    * wait for all pods to be ready
        + ```shell
          # NOTE: wait command will be blocked by pods named `tekton-resource-pruner-...` which scheduled by jobs
          kubectl -n tekton-pipelines get pod
          ```

## test

1. check connection of `tekton-dashboard`
    * ```shell
      curl --insecure --header 'Host: tekton-dashboard.local' https://localhost
      ```
2. visit `https://tekton-dashboard.local`
3. test `task`
    * prepare [tekton.task.yaml](resources/tekton/tekton.task.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/busybox:1.33.1-uclibc"
          ```
    * apply task(s)
        + ```shell
          kubectl -n tekton-pipelines apply -f tekton.task.yaml
          ```
    * prepare [tekton.build.task.run.yaml](resources/tekton/tekton.build.task.run.yaml.md)
    * run build task
        + ```shell
          kubectl -n tekton-pipelines create -f tekton.build.task.run.yaml
          ```
    * prepare [tekton.publish.task.run.yaml](resources/tekton/tekton.publish.task.run.yaml.md)
    * run publish task
        + ```shell
          kubectl -n tekton-pipelines create -f tekton.publish.task.run.yaml
          ```
4. test `pipeline`
    * prepare [tekton.pipeline.yaml](resources/tekton/tekton.pipeline.yaml.md)
    * apply pipeline
        + ```shell
          kubectl -n tekton-pipelines apply -f tekton.pipeline.yaml
          ```
    * prepare [tekton.pipeline.run.yaml](resources/tekton/tekton.pipeline.run.yaml.md)
    * run publish task
        + ```shell
          kubectl -n tekton-pipelines create -f tekton.pipeline.run.yaml
          ```

## uninstallation

1. uninstall `tekton`
    * ```shell
      kubectl -n tekton-operator delete -f tekton.config.yaml
      kubectl -n tekton-operator delete -f tekton_operator_v0.54.0_release.yaml
      ```