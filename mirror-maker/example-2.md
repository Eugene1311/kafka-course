# Копирование данных топика из одного кластера во второй с удалением сообщений

## Запускаем сервисы
```shell
docker compose up -d
``` 
## Создаём топик topic-to-copy на первом кластере
```shell
kafka-topics --create --bootstrap-server kafka1:9092 --replication-factor 3 --partitions 3 --topic topic-to-copy
```
## Отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --topic topic-to-copy --property parse.key=true --property key.separator=: \
    --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092
``` 
## Удалим первые 2 сообщения из топика на первом кластере
```shell
docker cp deleteme1.json kafka-course-mirror-maker-kafka1-1:/tmp/deleteme1.json
kafka-delete-records --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --offset-json-file /tmp/deleteme1.json
```
## Проверим содержимое топика на первом кластере и что сообщения удалились
```shell
kafka-console-consumer --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true  \
    --bootstrap-server kafka1:9092
``` 
## Запускаем MirrorMaker2 на втором кластере
```shell
KAFKA_LOG4J_OPTS="-Dlog4j.configuration=file:/config/connect-log4j.properties" &&
connect-mirror-maker -daemon /config/mm.properties
```
## Проверяем, что процесс с MirrorMaker2 запущен
```shell
jps -v
```
## Выводим содержимое топиков на обоих кластерах и сравним их смещения
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true
```
```shell
kafka-console-consumer --topic src.topic-to-copy --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092 \
    --from-beginning --property print.offset=true --property print.partition=true
```
## Отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --topic topic-to-copy --property parse.key=true --property key.separator=: \
    --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092
```
## Проверяем копирование сообщений во второй кластер и смещения сообщений

## Ещё раз удалим сообщения из топика на первом кластере
```shell
docker cp deleteme2.json kafka-course-mirror-maker-kafka1-1:/tmp/deleteme2.json
kafka-delete-records --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --offset-json-file /tmp/deleteme2.json
```
## Выводим содержимое топиков на обоих кластерах и сравним их смещения
```shell
kafka-console-consumer --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true \
    --bootstrap-server kafka1:9092
```
```shell
kafka-console-consumer --topic src.topic-to-copy  --from-beginning --property print.offset=true --property print.partition=true \
    --bootstrap-server kafka1-m:9092
```
## Удаляем контейнеры и тома
```shell
docker compose down
docker volume prune -f
```
