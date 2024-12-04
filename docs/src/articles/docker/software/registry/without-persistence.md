# local container registry

1. ```shell
   podman run --rm --name registry -p 5000:5000 -d docker.io/library/registry:2
   ```
2. [testing](test.md)