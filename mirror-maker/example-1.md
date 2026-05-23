# Копирование данных топика из одного кластера во второй

## Запускаем сервисы
```shell
docker compose up -d
``` 
## Создаём топик topic-to-copy на первом кластере
```shell
kafka-topics --create --bootstrap-server kafka1:9092 --replication-factor 1 --partitions 3 --topic topic-to-copy
```
## Отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --bootstrap-server kafka1:9092 --topic topic-to-copy
```
## Проверим содержимое топика на первом кластере
```shell
kafka-console-consumer --topic topic-to-copy --bootstrap-server kafka1:9092 --from-beginning --property print.offset=true --property print.partition=true
```
## Проверяем отсутствие топиков на втором кластере
```shell
kafka-topics --list --bootstrap-server kafka1-m:9092
```
## Запускаем MirrorMaker2 на втором кластере
-- todo mount log4j file properly
```shell
KAFKA_LOG4J_OPTS="-Dlog4j.configuration=file:/config/connect-log4j.properties" &&
connect-mirror-maker -daemon /config/mm.properties
```
## Проверяем, что процесс с MirrorMaker2 запущен
```shell
jps -v
```
## Читаем содержимое топика на втором кластере
```shell
kafka-console-consumer --topic src.topic-to-copy --bootstrap-server kafka1-m:9092 \
    --from-beginning --property print.offset=true --property print.partition=true
```
## Во втором терминале отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --bootstrap-server kafka1:9092 --topic topic-to-copy
```
## Проверяем, что процесс сообщения скопированы во второй кластер
## Удаляем контейнеры и тома
```shell
docker compose down
docker volume prune -f
```
