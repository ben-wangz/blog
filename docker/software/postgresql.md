### postgresql

* ```shell
  podman run --rm --name postgresql \
      -p 5432:5432 \
      -e POSTGRES_PASSWORD=postgresql \
      -e PGDATA=/var/lib/postgresql/data/pgdata \
      -v $(pwd)/postgresql-data:/var/lib/postgresql/data \
      --add-host host.docker.internal:host-gateway \
      -d docker.io/library/postgres:15.2-alpine3.17
  ```