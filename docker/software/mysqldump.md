### mysqldump

* backup database
    + ```shell
      docker run -it mysql:8.0.25 mysqldump \
          -h target.database.host.loccal \
          -P 3006 \
          -u root \
          -p$MYSQL_ROOT_PASSWORD \
          --all-databases \
          | gzip > db.sql.$(date +%s_%Y%m%d_%H_%M_%S).gz
      ```
* import a database from another
    + ```shell
      mysqldump \
          -u root \
          -p$MYSQL_ROOT_PASSWORD database_name \
          | mysql -h remote_target_database_host -u root -p remote_database_name
      ```