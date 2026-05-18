# Настройка SSL

## Определения:
- keystore - хранилище ключей и сертификатов
- truststore - хранилище сертификатов, которым доверяет носитель хранилища
- ca-cert: сертификат центра сертификации (CA)
- ca-key: закрытый (private) ключ центра сертификации (CA)
- ca-password: ключевая фраза (passphrase) центра сертификации (CA)
- cert-file: экспортированный неподписанный сертификат сервера
- cert-signed: подписанный сертификат сервера

## Создаём SSL ключ и сертификат для каждого брокера и клиента
- Брокер localhost - создаём server.keystore.jks:
keytool -genkey \
  -keyalg RSA \
  -keystore server.keystore.jks \
  -keypass password \
  -alias localhost \
  -validity 365 \
  -storetype pkcs12 \
  -storepass password \
  -dname "CN=localhost,OU=Kafka,O=Shift,L=Saint-Petersburg,ST=Saint-Petersburg,C=RU"

- Клиент client - создаём client.keystore.jks:

keytool -genkey \
-keyalg RSA \
-keystore client.keystore.jks \
-keypass password \
-alias client \
-validity 365 \
-storetype pkcs12 \
-storepass password \
-dname "CN=client,OU=Clients,O=Shift,L=Saint-Petersburg,ST=Saint-Petersburg,C=RU"

## Создаём собственный центр авторизации
- Создаём центр сертификации, который представляет собой просто пару открытых ключей (ca-key) и сертификат (ca-cert), и он предназначен для подписи других сертификатов:
  openssl req -new -x509 -keyout ca-key -out ca-cert -nodes -days 365

- Добавляем сгенерированный центр сертификации в хранилище доверия клиентов client.truststore.jks, чтобы клиенты могли доверять этому центру сертификации:
  keytool -importcert -keystore client.truststore.jks -alias CARoot -file ca-cert

- Добавляем сгенерированный центр сертификации в хранилище доверия брокеров server.truststore.jks, чтобы брокеры могли доверять этому центру сертификации:
  keytool -importcert -keystore server.truststore.jks -alias CARoot -file ca-cert

## Подписываем все сертификаты в хранилище ключей с помощью созданного центра сертификации
- Экспортируем сертификат из хранилища ключей:
  keytool -certreq -keystore server.keystore.jks -alias localhost -file cert-file
  keytool -certreq -keystore client.keystore.jks -alias client -file cert-client-file

- Подписываем его в центре сертификации:
  openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-file -out cert-signed -days 365 -CAcreateserial -passin pass:password
  openssl x509 -req -CA ca-cert -CAkey ca-key -in cert-client-file -out cert-client-signed -days 365 -CAcreateserial -passin pass:password

- Импортируем сертификат центра сертификации и подписанный сертификат в хранилище ключей брокера:
  keytool -importcert -keystore server.keystore.jks -alias CARoot -file ca-cert
  keytool -importcert -keystore server.keystore.jks -alias localhost -file cert-signed

- Импортируем сертификат центра сертификации и подписанный сертификат в хранилище ключей клиента:
  keytool -importcert -keystore client.keystore.jks -alias CARoot -file ca-cert
  keytool -importcert -keystore client.keystore.jks -alias client -file cert-client-signed

- Проверяем:
  keytool -list -v -keystore server.keystore.jks
  keytool -list -v -keystore client.keystore.jks


# Подключение по SSL без аутентификации

## Настраиваем брокер
- Создаём файл server-ssl.properties

ssl.keystore.location=./certs/server.keystore.jks
ssl.keystore.password=password
ssl.key.password=password
ssl.truststore.location=./certs/server.truststore.jks
ssl.truststore.password=password
security.inter.broker.protocol=SSL
ssl.client.auth=requested
ssl.endpoint.identification.algorithm=

## Настраиваем клиента
- Создаём файл client-ssl.properties

security.protocol=SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password

## Запускаем ZooKeeper и брокер Kafka с SSL
zookeeper-server-start.sh path/to/config/zookeeper.properties
kafka-server-start.sh configs/server-ssl.properties

## Подключаемся клиентом по SSL (из директории security)
kafka-topics.sh --list --bootstrap-server localhost:9093 --command-config configs/client-ssl.properties
kafka-topics.sh --create --topic test --bootstrap-server localhost:9093 --command-config configs/client-ssl.properties
kafka-topics.sh --list --bootstrap-server localhost:9093 --command-config configs/client-ssl.properties               
kafka-console-producer.sh --bootstrap-server localhost:9093 --topic test --producer.config configs/client-ssl.properties
kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic test --consumer.config configs/client-ssl.properties --from-beginning
^C

## Проверяем конфигурацию брокера
kafka-configs.sh --bootstrap-server localhost:9093 --entity-type brokers --entity-name 0 --describe --all --command-config configs/client-ssl.properties | grep -i ssl | sort

## Останавливаем брокер и ZooKeeper


# Подключение по SSL с аутентификацией

## Настраиваем брокер
- Создаём файл server-ssl-auth.properties

listeners=SSL://localhost:9093
ssl.keystore.location=/opt/kafka/private/server.keystore.jks
ssl.keystore.password=password
ssl.key.password=password
ssl.truststore.location=/opt/kafka/private/server.truststore.jks
ssl.truststore.password=password
security.inter.broker.protocol=SSL
ssl.client.auth=required
ssl.endpoint.identification.algorithm=

## Настраиваем клиента
- Создаём файл client-ssl-auth.properties

security.protocol=SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password
ssl.keystore.location=client.keystore.jks
ssl.keystore.password=password
ssl.key.password=password

## Запускаем ZooKeeper и брокер Kafka с SSL
zookeeper-server-start.sh path/to/config/zookeeper.properties
kafka-server-start.sh configs/server-ssl-auth.properties

## Подключаемся клиентом по SSL
- Попробуем подключиться без сертификата
  kafka-topics.sh --list --bootstrap-server localhost:9093 --command-config configs/client-ssl.properties

В подключении отказано: Received fatal alert: bad_certificate

- Подключаемся с сертификатом
  kafka-topics.sh --list --bootstrap-server localhost:9093 --command-config configs/client-ssl-auth.properties
  kafka-console-producer.sh --bootstrap-server localhost:9093 --topic test --producer.config configs/client-ssl-auth.properties
  kafka-console-consumer.sh --bootstrap-server localhost:9093 --topic test --consumer.config configs/client-ssl-auth.properties -from-beginning
  ^C

## Проверяем конфигурацию брокера
kafka-configs.sh --bootstrap-server localhost:9093 --entity-type brokers --entity-name 0 --describe --all --command-config configs/client-ssl-auth.properties | grep -i ssl | sort

## Останавливаем брокер и ZooKeeper


# SASL

## Настраиваем и запускаем ZooKeeper

- Создаём файл zookeeper-sasl.properties
  authProvider.sasl=org.apache.zookeeper.server.auth.SASLAuthenticationProvider

- Создаём файл zookeeper_jaas.conf
  Server {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  user_super="admin-secret"
  user_kafka="kafka-secret";
  };

- Запускаем ZooKeeper (из директории security)
  KAFKA_OPTS="-Djava.security.auth.login.config=configs/zookeeper_jaas.conf" zookeeper-server-start.sh configs/zookeeper-sasl.properties


## Подключение к брокеру Kafka по SASL_SSL
### Настраиваем и запускаем брокер
- Создаём файл server-sasl-ssl.properties

listeners=SSL://localhost:9093,SASL_SSL://localhost:9094
security.inter.broker.protocol=SSL
ssl.client.auth=required
ssl.keystore.location=certs/server.keystore.jks
ssl.keystore.password=password
ssl.key.password=password
ssl.truststore.location=/opt/kafka/private/server.truststore.jks
ssl.truststore.password=password
ssl.endpoint.identification.algorithm=
sasl.enabled.mechanisms=PLAIN

- Создаём файл kafka_server_jaas.conf
  KafkaServer {
  org.apache.kafka.common.security.plain.PlainLoginModule required
  username="kafkabroker"
  password="kafkabroker-secret"
  user_kafkabroker="kafkabroker-secret"
  user_kafka-broker-metric-reporter="kafkabroker-metric-reporter-secret"
  user_client="client-secret";
};

Client {
  org.apache.zookeeper.server.auth.DigestLoginModule required
  username="kafka"
  password="kafka-secret";
};

- Запускаем брокер Kafka
  KAFKA_OPTS="-Djava.security.auth.login.config=configs/kafka_server_jaas.conf" kafka-server-start.sh configs/server-sasl-ssl.properties

### Настраиваем клиента
- Создаём файл client-sasl-ssl.properties

security.protocol=SASL_SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="client" \
password="client-secret";

### Подключаемся клиентом по SSL с аутентификацией SASL
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-sasl-ssl.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-sasl-ssl.properties
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-sasl-ssl.properties -from-beginning

### Останавливаем брокер


## Подключение к брокеру Kafka по SASL_PLAINTEXT
### Настраиваем и запускаем брокер
- Создаём файл server-sasl-plain.properties

listeners=SASL_PLAINTEXT://localhost:9094
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN

- Запускаем брокер Kafka
  KAFKA_OPTS="-Djava.security.auth.login.config=configs/kafka_server_jaas.conf" kafka-server-start.sh configs/server-sasl-plain.properties

### Настраиваем клиента
- Создаём файл client-sasl-plain.properties

security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="client" \
password="client-secret";

### Подключаемся клиентом с аутентификацией SASL_PLAINTEXT
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-sasl-plain.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-sasl-plain.properties
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-sasl-plain.properties -from-beginning
^C

### Останавливаем брокер и ZooKeeper


# ACL с аутентификацией SASL_PLAINTEXT
## Настроиваем брокер
- Создаём файл server-acl-plain.properties

listeners=SASL_PLAINTEXT://localhost:9094
security.inter.broker.protocol=SASL_PLAINTEXT
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
allow.everyone.if.no.acl.found=true
super.users=User:client

## Запускаем ZooKeeper и брокер Kafka
KAFKA_OPTS="-Djava.security.auth.login.config=configs/zookeeper_jaas.conf" zookeeper-server-start.sh configs/zookeeper-sasl.properties
KAFKA_OPTS="-Djava.security.auth.login.config=configs/kafka_server_jaas.conf" kafka-server-start.sh configs/server-acl-plain.properties

## Подключаемся клиентом с аутентификацией SASL_PLAINTEXT и создаём ACL
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-sasl-plain.properties
kafka-acls.sh --bootstrap-server localhost:9094 --list --command-config configs/client-sasl-plain.properties
kafka-acls.sh --bootstrap-server localhost:9094 --add --allow-principal User:A2 --operation Write --topic test --command-config configs/client-sasl-plain.properties
kafka-acls.sh --bootstrap-server localhost:9094 --add --allow-principal User:Alice --operation Read --topic test --command-config configs/client-sasl-plain.properties
kafka-acls.sh --bootstrap-server localhost:9094 --list --command-config configs/client-sasl-plain.properties

## Создаём конфигурационные файлы для пользователей
- Файл client-alice-plain.properties

security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="Alice" \
password="Alice-secret";

- Файл client-bob-plain.properties

security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="Bob" \
password="Bob-secret";

- Файл client-a2-plain.properties

security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="A2" \
password="A2-secret";

## Проверяем права пользователей на просмотр списка тем, чтение и запись в темы
-- Список тем
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-bob-plain.properties
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-alice-plain.properties
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-a2-plain.properties

-- Запись в тему
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-bob-plain.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-alice-plain.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-a2-plain.properties

-- Чтение из темы
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-bob-plain.properties -from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-alice-plain.properties -from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-a2-plain.properties -from-beginning

## Останавливаем брокер и ZooKeeper


# ACL с аутентификацией SASL_SSL
## Настроиваем брокер
- Создаём файл server-acl-ssl.properties

listeners=SSL://localhost:9093,SASL_SSL://localhost:9094
security.inter.broker.protocol=SSL
ssl.keystore.location=./certs/server.keystore.jks
ssl.keystore.password=password
ssl.key.password=password
ssl.truststore.location=./certs/server.truststore.jks
ssl.truststore.password=password
ssl.client.auth=requested
ssl.endpoint.identification.algorithm=
sasl.enabled.mechanisms=PLAIN
authorizer.class.name=kafka.security.authorizer.AclAuthorizer
allow.everyone.if.no.acl.found=true
super.users=User:client

## Запускаем ZooKeeper и брокер Kafka
KAFKA_OPTS="-Djava.security.auth.login.config=configs/zookeeper_jaas.conf" zookeeper-server-start.sh configs/zookeeper-sasl.properties
KAFKA_OPTS="-Djava.security.auth.login.config=configs/kafka_server_jaas.conf" kafka-server-start.sh configs/server-acl-ssl.properties

## Подключаемся клиентом с аутентификацией SASL_SSL и создаём ACL
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-sasl-ssl.properties
kafka-acls.sh --bootstrap-server localhost:9094 --list --command-config configs/client-sasl-ssl.properties
kafka-acls.sh --bootstrap-server localhost:9094 --add --allow-principal User:Bob --operation Write --topic test --command-config configs/client-sasl-ssl.properties
kafka-acls.sh --bootstrap-server localhost:9094 --add --allow-principal User:Alice --operation Read --topic test --command-config configs/client-sasl-ssl.properties
kafka-acls.sh --bootstrap-server localhost:9094 --list --command-config configs/client-sasl-ssl.properties

## Создаём конфигурационные файлы для пользователей
- Файл client-alice-ssl.properties

security.protocol=SASL_SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="Alice" \
password="Alice-secret";

- Файл client-bob-ssl.properties

security.protocol=SASL_SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="Bob" \
password="Bob-secret";

- Файл client-a2-ssl.properties

security.protocol=SASL_SSL
ssl.truststore.location=client.truststore.jks
ssl.truststore.password=password
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
username="A2" \
password="A2-secret";

## Проверяем права пользователей на просмотр списка тем, чтение и запись в темы
-- Список тем
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-bob-ssl.properties
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-alice-ssl.properties
kafka-topics.sh --list --bootstrap-server localhost:9094 --command-config configs/client-a2-ssl.properties

-- Запись в тему
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-bob-ssl.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-alice-ssl.properties
kafka-console-producer.sh --bootstrap-server localhost:9094 --topic test --producer.config configs/client-a2-ssl.properties

-- Чтение из темы
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-bob-ssl.properties -from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-alice-ssl.properties -from-beginning
kafka-console-consumer.sh --bootstrap-server localhost:9094 --topic test --consumer.config configs/client-a2-ssl.properties -from-beginning

## Останавливаем брокер и ZooKeeper
