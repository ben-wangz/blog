FROM docker.io/library/alpine:3.19.0
RUN apk add aria2

COPY --chmod=744 entrypoint.sh /app/entrypoint.sh
CMD ["/app/entrypoint.sh"]