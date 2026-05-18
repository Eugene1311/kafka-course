# Example taken from here: https://kafka.apache.org/42/operations/tiered-storage/

#### Create topic
```shell
kafka-topics --create --topic tieredTopic --bootstrap-server localhost:9092 \
    --config remote.storage.enable=true --config local.retention.ms=1000 --config retention.ms=3600000 \
    --config segment.bytes=1048576 --config file.delete.delay.ms=1000
```

#### Send messages
```shell
kafka-producer-perf-test --topic tieredTopic --num-records 1000000 --record-size 1024 --throughput -1 \
 --producer-props bootstrap.servers=kafka:29092 --print-metrics
```

#### Consume message
```shell
kafka-console-consumer --topic tieredTopic --from-beginning --max-messages 1 --bootstrap-server localhost:9092 --property print.offset=true
```
