# JdbcSourceConnector с ksqlDB
## Проверяем статус Kafka Connect и плагины коннекторов
```shell
curl http://localhost:8083 | jq
```
## Проверяем наличие плагина 'io.confluent.connect.jdbc.JdbcSourceConnector'
```shell
curl http://localhost:8083/connector-plugins | jq
```
## Создадим таблицу в PostgresSQL и запишем в неё данные
```shell
CREATE TABLE series (id SERIAL PRIMARY KEY, title VARCHAR(120));
INSERT INTO series (title) values ('Stranger Things');
INSERT INTO series (title) values ('Black Mirror');
INSERT INTO series (title) values ('The Office');
````
## Запускаем ksqlDB CLI в контейнере ksqldb-server
```shell
ksql http://ksqldb-server:8088
```
## Устанавливаем чтение с начала
```shell
SET 'auto.offset.reset' = 'earliest';
```
## Создадим коннектор postgres-source
```shell
CREATE SOURCE CONNECTOR `postgres-source` WITH (
   "connector.class" = 'io.confluent.connect.jdbc.JdbcSourceConnector',
   "connection.url" = 'jdbc:postgresql://postgres:5432/postgres?user=postgres&password=password',
   "mode" = 'incrementing',
   "incrementing.column.name" = 'id',
   "table.whitelist" = 'series',
   "topic.prefix" = 'postgres.',
   "key" = 'id');
```
## Выведем список коннекторов
```shell
SHOW CONNECTORS;
```
## Получаем описание коннектора postgres-source
```shell
DESCRIBE CONNECTOR `postgres-source`;
```
## Проверяем топик 'postgres.series'
```shell
SHOW TOPICS;
```
## Выведем содержимое топика
```shell
PRINT `postgres.series` FROM BEGINNING;
```
## Создаём поток, который читает топик postgres.series
```shell
CREATE STREAM series WITH (kafka_topic = 'postgres.series', value_format = 'avro');
```
## Выведем описание потока
```shell
DESCRIBE series EXTENDED;
```
## Прочитаем записи (push-запрос)
```shell
SELECT * FROM series EMIT CHANGES;
```
## Добавим записи в таблицу БД
```shell
INSERT INTO series (title) values ('Four');
INSERT INTO series (title) values ('Five');
INSERT INTO series (title) values ('Six');
```
## Проверим новые события в потоке series
