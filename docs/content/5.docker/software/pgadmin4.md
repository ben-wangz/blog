### pgadmin4

* ```shell
  podman run --rm \
    -p 8080:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=ben.wangz@foxmail.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=123456' \
    -d docker.io/dpage/pgadmin4:6.15
  ```
* visit http://localhost:8080
