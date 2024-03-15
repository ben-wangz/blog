# neo4j

## server

* ```shell
  mkdir -p neo4j/data
  podman run --rm \
      --name neo4j \
      -p 7474:7474 \
      -p 7687:7687 \
      -e neo4j_ROOT_PASSWORD=mysql \
      -v $(pwd)/neo4j/data:/data \
      -d docker.io/library/neo4j:5.18.0-community-bullseye
  ```

* default credentials for http://localhost:7474
    + username: neo4j
    + password: neo4j
