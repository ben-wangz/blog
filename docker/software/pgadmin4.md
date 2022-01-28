### pgadmin4

* ```shell
  docker run --rm \
    -p 8080:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=ben.wangz@foxmail.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=123456' \
    -d dpage/pgadmin4:6.4
  ```
* visit http://localhost:8080