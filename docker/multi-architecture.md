## docker with multi-architecture

### how to run images with other architecture

1. reference: [qemu-user-static](https://github.com/multiarch/qemu-user-static)
2. requirements: only support x86_64 architectures
    * you can use qemu to emulate a machine with x86_64
3. we cannot run other platform images
    * NOTE: it's not true for docker desktop
    * for example
        + ```shell
          # which may return "x86_64"
          uname -m
          # we cannot run image arm64v8/ubuntu which require arm64v8 cpu
          # which will return "standard_init_linux.go:228: exec user process caused: exec format error"
          docker run --rm -t arm64v8/ubuntu uname -m
          ```
4. activate `multiarch/qemu-user-static`
    * ```shell
      docker run --rm --privileged multiarch/qemu-user-static --reset -p yes
      ```
5. we can run other platform images now
    * ```shell
      docker run --rm -t arm64v8/ubuntu uname -m
      docker run --rm -t arm32v6/alpine uname -m
      docker run --rm -t ppc64le/debian uname -m
      docker run --rm -t s390x/ubuntu uname -m
      docker run --rm -t arm64v8/fedora uname -m
      docker run --rm -t arm32v7/centos uname -m
      docker run --rm -t ppc64le/busybox uname -m
      docker run --rm -t i386/ubuntu uname -m
      ```
    * with `--platform` option
    * ```shell
      docker run --rm --platform linux/386 alpine:3.13.6 uname -m
      docker run --rm --platform linux/amd64 alpine:3.13.6 uname -m
      docker run --rm --platform linux/arm/v6 alpine:3.13.6 uname -m
      docker run --rm --platform linux/arm/v7 alpine:3.13.6 uname -m
      docker run --rm --platform linux/arm64/v8 alpine:3.13.6 uname -m
      docker run --rm --platform linux/ppc64le alpine:3.13.6 uname -m
      docker run --rm --platform linux/s390x alpine:3.13.6 uname -m
      ```

### how to build images with other architecture

1. check your buildx environment
    * ```shell
      docker buildx ls
      ```
    * in general, you will see something like this
        + ```text
          NAME/NODE       DRIVER/ENDPOINT STATUS  PLATFORMS
          desktop-linux   docker
            desktop-linux desktop-linux   running linux/arm64, linux/amd64, linux/riscv64, linux/ppc64le, linux/s390x, linux/386, linux/arm/v7, linux/arm/v6
          default *       docker
            default       default         running linux/arm64, linux/amd64, linux/riscv64, linux/ppc64le, linux/s390x, linux/386, linux/arm/v7, linux/arm/v6
          ```
2. create a new builder and use the builder created
    * ```shell
      docker buildx create --name mybuilder --use \
          --driver-opt network=host,image=moby/buildkit:buildx-stable-1
      ```
    * `-driver-opt network=host` is used for pushing the images to local docker registry
    * `--driver-opt image=moby/buildkit:buildx-stable-1` is used for specifying the buildx container image, whose
      default value is `moby/buildkit:buildx-stable-1`
    * you can use `docker buildx ls` to check buildx environment again
3. pull the stable buildkit image and inspect the builder used
    * ```shell
      IMAGE="moby/buildkit:buildx-stable-1"
      docker image inspect $IMAGE || docker pull $IMAGE
      docker buildx inspect --bootstrap
      ```
    * a buildx container will be created after running the inspect command
4. create a dockerfile to test
    * ```shell
      cat > Dockerfile <<EOF
      FROM ubuntu:20.04
      RUN apt-get update && apt-get install -y curl
      EOF
      ```
5. you will need a docker registry to be a proxy
    * optional, you can use the registry for your docker hub account
    * ```shell
      DOCKER_REGISTRY_STORAGE=$HOME/opt/buildx/docker-registry
      mkdir -p $DOCKER_REGISTRY_STORAGE
      docker run --rm --name docker-registry \
          -p 5000:5000 \
          -v $DOCKER_REGISTRY_STORAGE:/var/lib/registry \
          -d registry:2.7.1
      ```
6. build images for multi-architecture
    * ```shell
      docker buildx build --platform linux/amd64,linux/arm64,linux/arm/v7 -t localhost:5000/buildx-test:latest --push .
      ```
    * set a proper prefix, for example `docker.io/` of the image name instead of `localhost:5000` according to your
      docker registry
7. verify the image pushed
    * install jq if not installed
        + mac: `brew install jq`
        + centos 8: `dnf -y install jq`
    * ```shell
      IMAGE=localhost:5000/buildx-test:latest
      for ARCH in "linux/arm64" "linux/amd64" "linux/arm/v7";
      do
          docker pull --platform $ARCH $IMAGE 
          docker image inspect $IMAGE | jq '.[0]|[.Os, .Architecture, .Variant] | join("/")'
      done
      ```
    * expected output is something like
        + ```text
          latest: Pulling from buildx-test
          Digest: sha256:10e513599986d9ca84496d134545a31f0f8aa26236ad4fde4dc76188d676dbc9
          Status: Downloaded newer image for localhost:5000/buildx-test:latest
          localhost:5000/buildx-test:latest
          "linux/arm64/"
          latest: Pulling from buildx-test
          Digest: sha256:10e513599986d9ca84496d134545a31f0f8aa26236ad4fde4dc76188d676dbc9
          Status: Downloaded newer image for localhost:5000/buildx-test:latest
          localhost:5000/buildx-test:latest
          "linux/amd64/"
          latest: Pulling from buildx-test
          Digest: sha256:10e513599986d9ca84496d134545a31f0f8aa26236ad4fde4dc76188d676dbc9
          Status: Downloaded newer image for localhost:5000/buildx-test:latest
          localhost:5000/buildx-test:latest
          "linux/arm/v7"
          ```
8. delete the builder
    * ```shell
      docker buildx rm mybuilder
      ```
