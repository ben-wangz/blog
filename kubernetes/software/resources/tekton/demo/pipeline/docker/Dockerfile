FROM docker.io/alpine:3.15.0
ARG ALPINE_REPO=mirrors.aliyun.com

RUN set -x \
    && sed -i "s/dl-cdn.alpinelinux.org/${ALPINE_REPO}/g" /etc/apk/repositories \
    && apk update \
    && apk add apache2-utils \
    && rm -rf /var/cache/apk/*

ENTRYPOINT ["htpasswd", "-Bbn"]