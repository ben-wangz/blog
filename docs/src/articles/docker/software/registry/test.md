# tests for container registry

1. ```shell
   podman pull docker.io/library/alpine:latest
   podman tag docker.io/library/alpine:latest localhost:5000/alpine:latest
   podman push --tls-verify=false localhost:5000/alpine:latest
   ```
2. ```shell
   podman image rm docker.io/library/alpine:latest localhost:5000/alpine:latest
   podman pull --tls-verify=false localhost:5000/alpine:latest
   ```
