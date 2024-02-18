# mairadb

## server

* ```shell
  mkdir -p mariadb/data
  podman run --rm \
      -p 3306:3306 \
      -e MARIADB_ROOT_PASSWORD=mysql \
      -v $(pwd)/mariadb/data:/var/lib/mysql \
      -d docker.io/library/mariadb:11.2.2-jammy
  ```

### web console

* ```shell
  podman run --rm -p 8080:80 \
      -e PMA_ARBITRARY=1 \
      -d docker.io/library/phpmyadmin:5.1.1-apache
  ```

* visit http://localhost:8080

### mysqldump

* backup database
    + ```shell
      podman run -it docker.io/library/mysql:8.0.25 mysqldump \
          -h target.database.host.loccal \
          -P 3006 \
          -u root \
          -p$MYSQL_ROOT_PASSWORD \
          --column-statistics=0 \
          --all-databases \
          | gzip > db.sql.$(date +%s_%Y%m%d_%H_%M_%S).gz
      ```
    + ```shell
      podman run -it docker.io/library/mysql:8.0.25 mysqldump \
          -h target.database.host.loccal \
          -P 3006 \
          -u root \
          -p$MYSQL_ROOT_PASSWORD \
          --column-statistics=0 \
          my_db_name \
          | gzip > db.sql.$(date +%s_%Y%m%d_%H_%M_%S).gz
      ```
* import a database from another
    + ```shell
      mysqldump \
          -u root \
          -p$MYSQL_ROOT_PASSWORD \
          --column-statistics=0 \
          database_name \
          | mysql -h remote_target_database_host -u root -p remote_database_name
      ```