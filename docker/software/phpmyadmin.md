### phpmyadmin

* ```shell
  docker run --rm -p 8080:80 -e PMA_ARBITRARY=1 -d phpmyadmin:5.1.1-apache
  ```
* visit http://localhost:8080