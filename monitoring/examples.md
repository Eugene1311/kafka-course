# Simulate under-replicated partitions
## Disable JMX
```shell
unset KAFKA_OPTS && unset KAFKA_JMX_OPTS
```
## Create topic with 1 partition and 3 replicas
```shell
kafka-topics --bootstrap-server kafka-3:29092 --create --topic test --replication-factor 3
```
## Check under-replicated partitions
```shell
kafka-topics --bootstrap-server kafka-3:29092 --describe --under-replicated-partitions
```
## Stop one broker
## Check under-replicated partitions
```shell
kafka-topics --bootstrap-server kafka-3:29092 --describe --under-replicated-partitions
```

# Producer performance test
```shell
unset KAFKA_OPTS && unset KAFKA_JMX_OPTS
kafka-producer-perf-test --topic test --num-records 1000000 --record-size 1024 --throughput -1 \
 --producer-props bootstrap.servers=kafka-1:29092 --print-metrics
```

# Unbalance cluster
## Create 2 topics each one with 3 partitions with leaders at one broker
```shell
kafka-topics --create --topic test-1 --replica-assignment 2:3:1 --bootstrap-server kafka-1:29092
kafka-topics --create --topic test-2 --replica-assignment 2:3:1 --bootstrap-server kafka-1:29092
```

## Load topics with messages
```shell
kafka-producer-perf-test --topic test-1 --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props bootstrap.servers=kafka-1:29092
```
```shell
kafka-producer-perf-test --topic test-2 --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props bootstrap.servers=kafka-1:29092
```

## Reassign partitions for topic test-2
### Copy file to container
```shell
docker cp reassign.json kafka-course-monitoring-kafka-3-1:/data/reassign.json
```
```shell
kafka-reassign-partitions --execute --reassignment-json-file /data/reassign.json --bootstrap-server kafka-1:29092
```

# Configure segment size on topics
```shell
kafka-configs --bootstrap-server kafka-1:29092 \
  --entity-type topics --entity-name test-1 \
  --alter --add-config segment.bytes=5242880
```
```shell
kafka-configs --bootstrap-server kafka-1:29092 \
  --entity-type topics --entity-name test-2 \
  --alter --add-config segment.bytes=5242880
```

# Configure batch size for producer
## With default batch size
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set batch size to 100 Kb
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=102400 bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set batch size to 5 Kb
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=5120 bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set batch size to 1Mb
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=1073741824 bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set batch size to 500 Kb
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=512000 bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set batch size to 1 Kb
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=1024 bootstrap.servers=kafka-1:29092 --print-metrics
```

# Configure producer compression type
## Default value
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=102400 bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set compression.type to lz4
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=102400 compression.type=lz4 \
    bootstrap.servers=kafka-1:29092 --print-metrics
```
## Set compression.type to gzip
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props batch.size=102400 compression.type=gzip \
    bootstrap.servers=kafka-1:29092 --print-metrics
```

# Acks all and min.insync.replicas 3
## Acks 1
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props acks=1 \
    bootstrap.servers=kafka-1:29092 --print-metrics
```
## Acks all
```shell
kafka-producer-perf-test --topic test --num-records 500000 --record-size 1024 --throughput -1 \
 --producer-props acks=-1 \
    bootstrap.servers=kafka-1:29092 --print-metrics
```

# Consumer perf test
```shell
kafka-consumer-perf-test --topic test --messages 1000000 --bootstrap-server kafka-1:29092 --print-metrics
```
## Check fetch.max.bytes effect
```shell
kafka-consumer-perf-test --topic test --messages 1000000 --fetch-size 102400 \
--bootstrap-server kafka-1:29092 --print-metrics
```
```shell
kafka-consumer-perf-test --topic test --messages 1000000 --fetch-size 1024 \
--bootstrap-server kafka-1:29092 --print-metrics
```
