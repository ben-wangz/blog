FROM docker.io/library/gradle:8.5.0-jdk11-jammy as builder

COPY . /app
WORKDIR /app
RUN ./gradlew :docs:npm_run_build

FROM docker.io/library/nginx:1.25.3-alpine-slim
COPY --from=builder /app/docs/.output /usr/share/nginx/html
