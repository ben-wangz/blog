# postgresql

## server

* ```shell
  mkdir $(pwd)/postgresql-data
  podman run --rm --name postgresql \
      -p 5432:5432 \
      -e POSTGRES_PASSWORD=postgresql \
      -e PGDATA=/var/lib/postgresql/data/pgdata \
      -v $(pwd)/postgresql-data:/var/lib/postgresql/data \
      -d docker.io/library/postgres:15.2-alpine3.17
  ```

## web console

* ```shell
  podman run --rm \
    -p 8080:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=ben.wangz@foxmail.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=123456' \
    -d docker.io/dpage/pgadmin4:6.15
  ```
* visit http://localhost:8080
