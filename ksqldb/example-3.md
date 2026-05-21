# PostgresConnector с ksqlDB
## Создадим таблицу в PostgresSQL и запишем в неё данные
```shell
CREATE TABLE customers (id INT PRIMARY KEY, name TEXT, age INT);
INSERT INTO customers (id, name, age) VALUES (5, 'Fred', 34);
INSERT INTO customers (id, name, age) VALUES (7, 'Sue', 25);
INSERT INTO customers (id, name, age) VALUES (2, 'Bill', 51);
```

## Запускаем ksqlDB CLI в контейнере ksqldb-server
```shell
ksql http://ksqldb-server:8088
```
## Устанавливаем чтение с начала
```shell
SET 'auto.offset.reset' = 'earliest';
```

## Создаём коннектор customers_source
```shell
CREATE SOURCE CONNECTOR `customers_source` WITH (
   'connector.class' = 'io.debezium.connector.postgresql.PostgresConnector',
   'database.hostname' = 'postgres',
   'database.port' = '5432',
   'database.user' = 'postgres',
   'database.password' = 'password',
   'database.dbname' = 'postgres',
   'database.server.name' = 'postgres',
   'table.include.list' = 'public.customers',
   'topic.prefix' = 'postgres',
   'key.converter' = 'org.apache.kafka.connect.json.JsonConverter',
   'key.converter.schemas.enable' = 'false',
   'transforms' = 'unwrap',
   'transforms.unwrap.type' = 'io.debezium.transforms.ExtractNewRecordState',
   'transforms.unwrap.drop.tombstones' = 'true',
   'transforms.unwrap.delete.handling.mode' = 'rewrite',
   'plugin.name' = 'pgoutput',
   'tasks.max' = '1'
);
```

## Выводим список коннекторов
```shell
SHOW CONNECTORS;
```

## Получаем описание коннектора inventory-connector
```shell
DESCRIBE CONNECTOR `customers_source`;
```

## Проверяем топики
```shell
SHOW TOPICS;
```

## Выведем содержимое топика
```shell
PRINT `postgres.public.customers` FROM BEGINNING;
```

## Создаём поток
```shell
CREATE STREAM customers WITH (kafka_topic = 'postgres.public.customers', value_format = 'avro');
```

## Выведем содержимое потока
```shell
SELECT * FROM customers EMIT CHANGES;
```

## Сделаем изменения в таблице БД
```shell
INSERT INTO customers (id, name, age) VALUES (3, 'Ann', 18);
UPDATE customers set age = 35 WHERE id = 5;
DELETE FROM customers WHERE id = 2;
```

## создаём таблицу customers_by_key
```shell
CREATE TABLE customers_by_key AS
   SELECT id,
   latest_by_offset(name, false) AS name,
   latest_by_offset(age, false) AS age
   FROM customers
   GROUP BY id
   EMIT CHANGES;
```


