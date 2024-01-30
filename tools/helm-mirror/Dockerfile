FROM docker.io/alpine:3.19.1

RUN apk update && apk add bash --no-cache
ARG YQ_VERSION=v4.40.5
ARG YQ_BINARY=yq_linux_amd64
RUN wget -O /usr/bin/yq https://github.com/mikefarah/yq/releases/download/${YQ_VERSION}/${YQ_BINARY} \
    && chmod a+x /usr/bin/yq
ARG MIRROR=
ARG HELM_VERSION=v3.7.0
RUN ARCH=$(uname -m) \
    && if [ "${ARCH}" = "x86_64" ]; then \
        ARCH_IN_FILE_NAME=linux-amd64; \
    elif [ "${ARCH}" = "aarch64" ]; then \
        ARCH_IN_FILE_NAME=linux-arm64; \
    else \
        echo "NOT SUPPORT: ${ARCH}"; \
    fi \
    && FILE_NAME=helm-${HELM_VERSION}-${ARCH_IN_FILE_NAME}.tar.gz \
    && wget -O ${FILE_NAME} "https://${MIRROR}get.helm.sh/${FILE_NAME}" \
    && tar zxf ${FILE_NAME} \
    && mkdir -p ${HOME}/bin \
    && mv -f ${ARCH_IN_FILE_NAME}/helm /usr/bin \
    && rm -rf ./${FILE_NAME} \
    && rm -rf ./${ARCH_IN_FILE_NAME} \
    && chmod u+x /usr/bin/helm

COPY --chmod=0555 mirror.sh /app/mirror.sh
ENTRYPOINT ["/bin/bash"]
CMD ["/app/mirror.sh"]
