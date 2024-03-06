ARG REGISTRY=docker.io
FROM $REGISTRY/library/gradle:8.5.0-jdk11-jammy as builder

COPY . /app
WORKDIR /app
RUN gradle :docs:npm_run_build

FROM $REGISTRY/bitnami/nginx:1.25.3-debian-11-r4
COPY --from=builder /app/docs/build/dist /app
