### show transaction info

```SQL
select 
    trx_id, 
    trx_started, 
    trx_wait_started, 
    trx_mysql_thread_id, 
    trx_query, 
    trx_operation_state, 
    trx_tables_locked 
from information_schema.INNODB_TRX;
```

### show process

```SQL
show full processlist;
```

### show open tables

```SQL
SHOW OPEN TABLES where `database` = 'my_database';
```
