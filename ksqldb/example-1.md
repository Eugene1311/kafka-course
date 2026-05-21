# Демо ksqlDB
## Запускаем ksqlDB CLI в контейнере ksqldb-server
```shell
ksql http://ksqldb-server:8088
```
## Устанавливаем чтение с начала
```shell
SET 'auto.offset.reset' = 'earliest';
```
## Создаём поток из топика 'users'
```shell
CREATE STREAM usersStream (ROWKEY INT KEY, USERNAME VARCHAR) WITH (KAFKA_TOPIC='users', VALUE_FORMAT='JSON', PARTITIONS=3);
```
## Проверяем его создание
```shell
SHOW STREAMS EXTENDED;
```
## Проверяем создание топика 'users'
```shell
SHOW TOPICS;
```
## Добавляем сообщения в поток
```shell
INSERT INTO usersStream VALUES (1, 'Alex');
INSERT INTO usersStream VALUES (2, 'Barbara');
INSERT INTO usersStream VALUES (3, 'Carl');
INSERT INTO usersStream VALUES (4, 'Fiona');
```
## Прочитаем записи (pull-запрос) в потоке
```shell
SELECT * FROM usersStream;
```
## Прочитаем записи в топике
```shell
PRINT users FROM BEGINNING;
```
## Создадим push-запрос
```shell
SELECT 'Hello, ' + USERNAME AS GREETING FROM usersStream EMIT CHANGES;
```
## Во втором терминале добавляем записи в поток
```shell
INSERT INTO usersStream VALUES (5, 'Mary');
INSERT INTO usersStream VALUES (6, 'Garry');
INSERT INTO usersStream VALUES (7, 'Ann');
```
## В первом терминале посмотрим на метаданные
```shell
^C
SELECT ROWTIME, ROWOFFSET, ROWPARTITION, * FROM usersStream EMIT CHANGES;
```
## Во втором терминале работаем с запросами
```shell
LIST QUERIES;
SHOW QUERIES EXTENDED;
```
## Работаем с функциями
```shell
SHOW FUNCTIONS;
DESCRIBE FUNCTION UUID;
```

# CSAS - Create Stream As Select
## В первом терминале создаём первый поток
```shell
CREATE STREAM PurchaseStream (id INT KEY, product VARCHAR, purchase_ts VARCHAR)
WITH (KAFKA_TOPIC='PurchaseTopic', VALUE_FORMAT='JSON', TIMESTAMP='purchase_ts', TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX', PARTITIONS=3);
```
## Создаём второй поток во втором терминале
```shell
CREATE STREAM PaymentStream (id INT KEY, purchaseId INT, status VARCHAR, payment_ts VARCHAR)
WITH (KAFKA_TOPIC='PaymentTopic', VALUE_FORMAT='JSON', TIMESTAMP='payment_ts', TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX', PARTITIONS=3);
```
## Создаём CSAS - Create Stream As Select
```shell
CREATE STREAM PaymentPurchaseStream
   WITH (KAFKA_TOPIC = 'PaymentPurchaseTopic', VALUE_FORMAT='JSON')
   AS SELECT purchase.id AS purchaseId, purchase.product, payment.status
   FROM PurchaseStream purchase
   INNER JOIN PaymentStream payment
   WITHIN 7 DAYS
   GRACE PERIOD 24 HOURS
   ON purchase.id = payment.purchaseId
   EMIT CHANGES;
```
## Добавим записи в потоки
```shell
INSERT INTO PurchaseStream (id, product, purchase_ts) VALUES (1, 'kettle', '2022-01-29T06:01:18Z');
INSERT INTO PurchaseStream (id, product, purchase_ts) VALUES (2, 'grill' , '2022-01-29T17:02:20Z');
INSERT INTO PurchaseStream (id, product, purchase_ts) VALUES (3, 'toaster', '2022-01-29T13:44:10Z');
INSERT INTO PurchaseStream (id, product, purchase_ts) VALUES (4, 'hair dryer', '2022-01-29T11:58:25Z');

INSERT INTO PaymentStream (id, purchaseId, status, payment_ts) VALUES (101, 1, 'OK', '2022-01-29T06:11:18Z');
INSERT INTO PaymentStream (id, purchaseId, status, payment_ts) VALUES (103, 3, 'OK', '2022-01-29T13:54:10Z');
INSERT INTO PaymentStream (id, purchaseId, status, payment_ts) VALUES (104, 4, 'OK', '2022-01-29T12:08:25Z');
```
## Проверяем объединённый поток
SELECT * FROM PaymentPurchaseStream;


## Агрегирование
## Создаём поток
```shell
CREATE STREAM OrderStream (ID INT KEY, status VARCHAR, ts VARCHAR) 
WITH (KAFKA_TOPIC='OrderTopic', VALUE_FORMAT='JSON', TIMESTAMP='ts', TIMESTAMP_FORMAT='yyyy-MM-dd''T''HH:mm:ssX', PARTITIONS=3);
```
## Создаём CTAS - Create Table As Select
```shell
CREATE TABLE OrderTable AS
   SELECT id, count(*) as count
   FROM OrderStream
   GROUP BY id
   EMIT CHANGES;
```
## Добавим записи в поток
```shell
INSERT INTO OrderStream VALUES (2, 'OK', '2022-01-29T06:11:18Z');
INSERT INTO OrderStream VALUES (3, 'OK', '2022-01-29T06:11:18Z');
INSERT INTO OrderStream VALUES (2, 'OK', '2022-01-29T13:54:10Z');
INSERT INTO OrderStream VALUES (4, 'OK', '2022-01-29T12:08:25Z');
INSERT INTO OrderStream VALUES (1, 'OK', '2022-01-29T06:11:18Z');
INSERT INTO OrderStream VALUES (2, 'OK', '2022-01-29T06:11:18Z');
INSERT INTO OrderStream VALUES (2, 'OK', '2022-01-29T13:54:10Z');
INSERT INTO OrderStream VALUES (3, 'OK', '2022-01-29T13:54:10Z');
```
## Посмотрим содержимое таблицы
SELECT * FROM OrderTable;
