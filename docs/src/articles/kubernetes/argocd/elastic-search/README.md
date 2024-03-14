# elastic-search

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. cert-manager is ready and the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `elastic-search.yaml`
    * ```yaml
      <!-- @include: elastic-search.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f elastic-search.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/elastic-search
      ```

## tests

* elastic-search.dev.geekcity.tech should be resolved to nginx-ingress
    + for example, add `$K8S_MASTER_IP elastic-search.dev.geekcity.tech` to `/etc/hosts`
* make a REST api call
    + ```shell
      curl -k "https://elastic-search.dev.geekcity.tech:32443/?pretty"
      ```
* add single document
    + ```shell
      curl -k -H "Content-Type: application/json" \
          -X POST "https://elastic-search.dev.geekcity.tech:32443/books/_doc?pretty" \
          -d '{"name": "Snow Crash", "author": "Neal Stephenson", "release_date": "1992-06-01", "page_count": 470}'
      ```
* bulk add documents
    + ```shell
      curl -k -H "Content-Type: application/x-ndjson" \
          -X POST "https://elastic-search.dev.geekcity.tech:32443/books/_bulk?pretty" \
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
      curl -k -X GET "https://elastic-search.dev.geekcity.tech:32443/books/_search?pretty"
      ```
* search with match query
    + ```shell
      curl -k -H "Content-Type: application/json" \
          -X GET "https://elastic-search.dev.geekcity.tech:32443/books/_search?pretty" \
          -d '{"query": {"match": {"name": "brave"}}}'
      ```
