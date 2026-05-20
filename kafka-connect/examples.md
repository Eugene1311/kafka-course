# JDBC Source Connector (https://docs.confluent.io/kafka-connectors/jdbc/current/source-connector/source_config_options.html#mode)
## Create table in PostgresDB copy data from file to it
```shell
CREATE TABLE clients
(
    id            int PRIMARY KEY,
    first_name    text,
    last_name     text,
    gender        text,
    card_number   text,
    bill          numeric(7, 2),
    created_date  timestamp,
    modified_date timestamp
);
COPY clients FROM '/data/clients.csv' WITH (FORMAT csv, HEADER true);
```

## Register connector
```shell
curl -X POST --data-binary "@clients-source-connector.json" -H "Content-Type: application/json" http://localhost:8083/connectors | jq
```

## Check that connector have created successfully
```shell
curl http://localhost:8083/connectors/clients-source-connector/status | jq
```

## Check topic 'clients' created
```shell
unset KAFKA_OPTS && unset KAFKA_JMX_OPTS && \
kafka-topics --list --bootstrap-server kafka:29092
```

## Add some entries to DB
```shell
insert into clients
values (1007, 'Averill', 'Fairtlough', 'Male', '4017957656366', 7389.39, '2022-12-06 07:34:04', now()),
       (1008, 'Averill', 'Fairtlough', 'Male', '4017957656366', 7389.39, '2022-12-06 07:34:04', now()),
       (1009, 'Averill', 'Fairtlough', 'Male', '4017957656366', 7389.39, '2022-12-06 07:34:04', now()),
       (1010, 'Averill', 'Fairtlough', 'Male', '4017957656366', 7389.39, '2022-12-06 07:34:04', now()),
       (1011, 'Averill', 'Fairtlough', 'Male', '4017957656366', 7389.39, '2022-12-06 07:34:04', now());
```

## Check new messages in topic 'clients'

# ElasticSearch Sink Connector (https://docs.confluent.io/kafka-connectors/elasticsearch/current/overview.html)
## Register connector
```shell
curl -X POST --data-binary "@clients-sink-connector.json" -H "Content-Type: application/json" http://localhost:8083/connectors | jq
```

## Check that connector have created successfully
```shell
curl http://localhost:8083/connectors/clients-sink-connector/status | jq
```

## Check 'clients' index inElasticSearch - http://localhost:9200/postgres.clients/_search?pretty

