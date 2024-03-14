# elastic-search

## server

```shell
mkdir -p elastic-search/data
chmod g+rwx elastic-search/data
podman run --rm \
    --ulimit nofile=65535:65535 \
    --group-add $(id -g) \
    --name elastic-search \
    -p 9200:9200 \
    -p 9300:9300 \
    -e "discovery.type=single-node" \
    -v $(pwd)/elastic-search/data:/usr/share/elasticsearch/data \
    -d docker.io/library/elasticsearch:8.12.2
```

## password

* reset password
  ```shell
  podman exec -it elastic-search \
      /usr/share/elasticsearch/bin/elasticsearch-reset-password -u elastic --silent --batch \
      > elastic-search/passwords.txt
  ```

## test with curl

* copy `http_ca.crt` from container
    + ```shell
      podman cp elastic-search:/usr/share/elasticsearch/config/certs/http_ca.crt elastic-search/http_ca.crt
      ```
* make a REST api call
    + ```shell
      ELASTIC_PASSWORD=$(cat elastic-search/passwords.txt | tr -d '\r')
      curl --cacert elastic-search/http_ca.crt -u "elastic:${ELASTIC_PASSWORD}" "https://localhost:9200?pretty"
      ```
* add single document
    + ```shell
      curl --cacert elastic-search/http_ca.crt \
          -u "elastic:${ELASTIC_PASSWORD}" \
          -H "Content-Type: application/json" \
          -X POST "https://localhost:9200/books/_doc?pretty" \
          -d '{"name": "Snow Crash", "author": "Neal Stephenson", "release_date": "1992-06-01", "page_count": 470}'
      ```
* bulk add documents
    + ```shell
      curl --cacert elastic-search/http_ca.crt \
          -u "elastic:${ELASTIC_PASSWORD}" \
          -H "Content-Type: application/x-ndjson" \
          -X POST "https://localhost:9200/books/_bulk?pretty" \
          -d '{ "index" : { "_index" : "books" } }
      {"name": "Revelation Space", "author": "Alastair Reynolds", "release_date": "2000-03-15", "page_count": 585}
      { "index" : { "_index" : "books" } }
      {"name": "1984", "author": "George Orwell", "release_date": "1985-06-01", "page_count": 328}
      { "index" : { "_index" : "books" } }
      {"name": "Fahrenheit 451", "author": "Ray Bradbury", "release_date": "1953-10-15", "page_count": 227}
      { "index" : { "_index" : "books" } }
      {"name": "Brave New World", "author": "Aldous Huxley", "release_date": "1932-06-01", "page_count": 268}
      { "index" : { "_index" : "books" } }
      {"name": "The Handmaids Tale", "author": "Margaret Atwood", "release_date": "1985-06-01", "page_count": 311}
      '
      ```
* search all documents with index named `books`
    + ```shell
      curl --cacert elastic-search/http_ca.crt \
          -u "elastic:${ELASTIC_PASSWORD}" \
          -X GET "https://localhost:9200/books/_search?pretty"
      ```
* search with match query
    + ```shell
      curl --cacert elastic-search/http_ca.crt \
          -u "elastic:${ELASTIC_PASSWORD}" \
          -H "Content-Type: application/json" \
          -X GET "https://localhost:9200/books/_search?pretty" \
          -d '{"query": {"match": {"name": "brave"}}}'
      ```
