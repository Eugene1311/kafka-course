# Копирование данных топика из одного кластера во второй

## Запускаем сервисы
```shell
docker compose up
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
## Проверяем список топиков на втором кластере
```shell
kafka-topics --list --bootstrap-server kafka1-m:9092
```
## Запускаем MirrorMaker2 на втором кластере
-- todo mount log4j file properly
```shell
connect-mirror-maker -daemon /tmp/config/mm.properties
jps
jps -v
```
## Проверяем список топиков на втором кластере
```shell
kafka-topics --list --bootstrap-server kafka1-m:9092
```
## Открываем два терминала
## В первом терминале проверяем содержимое топика на втором кластере
```shell
kafka-console-consumer --topic src.topic-to-copy --bootstrap-server kafka1-m:9092 --from-beginning --property print.offset=true --property print.partition=true
```
## Во втором терминале отправляем данные в топик topic-to-copy на первом кластере
```shell
docker exec -ti kafka1 /usr/bin/kafka-console-producer --bootstrap-server kafka1:9092 --topic topic-to-copy
```
## Удаляем контейнеры и тома
```shell
docker compose down
docker volume prune -f
```

# Копирование данных топика из одного кластера во второй с удалением сообщений

## Запускаем сервисы
```shell
docker compose up
``` 
## Создаём топик topic-to-copy на первом кластере
```shell
kafka-topics --create --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --replication-factor 3 --partitions 3 --topic topic-to-copy
```
## Выводим описание топика
```shell
kafka-topics --describe --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092
```
## Отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --bootstrap-server kafka1:9092 --topic my-topic --property parse.key=true --property key.separator=:
``` 
## Проверим содержимое топика на первом кластере
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic topic-to-copy \
--from-beginning --property print.offset=true --property print.partition=true
``` 
## Удалим первые сообщения из топика на первом кластере
```shell
docker cp deleteme1.json kafka-course-mirror-maker-kafka1-1:/tmp/deleteme1.json
kafka-delete-records --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --offset-json-file /tmp/deleteme1.json
``` 
## Проверим смещения в партициях топика на первом кластере
```shell
kafka-get-offsets --topic topic-to-copy --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092
``` 
## Проверим содержимое топика на первом кластере
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic topic-to-copy \
    --from-beginning --property print.offset=true --property print.partition=true
``` 
## Проверяем список топиков на втором кластере
```shell
kafka-topics --list --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092
```
## Запускаем MirrorMaker2 на втором кластере
```shell
connect-mirror-maker -daemon /tmp/config/mm.properties
```
## Проверяем список топиков на втором кластере
```shell
kafka-topics --list --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092
```
## Выводим содержимое топиков на обоих кластерах и сравним их смещения
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true
kafka-console-consumer --topic src.topic-to-copy --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092 \
    --from-beginning --property print.offset=true --property print.partition=true
```
## Открываем два терминала
## В первом терминале проверяем содержимое топика на втором кластере
```shell
kafka-console-consumer --topic src.topic-to-copy --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092 \
    --from-beginning --property print.offset=true --property print.partition=true
```
## Во втором терминале отправляем данные в топик topic-to-copy на первом кластере
```shell
kafka-console-producer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --topic topic-to-copy
```
## Останавливаем чтение из топика в первом терминале
## Выводим содержимое топиков на обоих кластерах и сравним их смещения
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true
kafka-console-consumer --topic src.topic-to-copy \
    --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092 --from-beginning --property print.offset=true --property print.partition=true
```
## Ещё раз удалим сообщения из топика на первом кластере
```shell
docker cp deleteme2.json kafka-course-mirror-maker-kafka1-1:/tmp/deleteme2.json
kafka-delete-records --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 --offset-json-file /tmp/deleteme2.json
```
## Выводим содержимое топиков на обоих кластерах и сравним их смещения
```shell
kafka-console-consumer --bootstrap-server kafka1:9092,kafka2:9092,kafka3:9092 \
    --topic topic-to-copy --from-beginning --property print.offset=true --property print.partition=true
kafka-console-consumer --topic src.topic-to-copy \
    --bootstrap-server kafka1-m:9092,kafka2-m:9092,kafka3-m:9092 --from-beginning --property print.offset=true --property print.partition=true
```
## Удаляем контейнеры и тома
```shell
docker compose down
docker volume prune -f
```
