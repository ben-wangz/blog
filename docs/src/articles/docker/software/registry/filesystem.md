# container registry with filesystem storage

1. ```shell
   mkdir -p data
   podman run --rm --name registry \
       -p 5000:5000 \
       -v $(pwd)/registry/data:/data \
       -e REGISTRY_STORAGE_FILESYSTEM_ROOTDIRECTORY=/data \
       -d docker.io/library/registry:2
   ```
2. [testing](test.md)