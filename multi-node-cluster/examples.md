# Start docker compose
```shell
docker-compose up -d
```

# Get into container
```shell
docker debug kafka-course-multi-node-cluster-kafka-1-1
```

# Create replicas on exact brokers:
Inside Docker container:
#### Disable javaagent and jmx for CLI and create topic
```shell
export KAFKA_OPTS= && export KAFKA_JMX_OPTS= &&
kafka-topics --create --topic test --replica-assignment 1:2:3 --bootstrap-server kafka-1:29092
```

# Read from follower
```shell
kafka-console-consumer --bootstrap-server kafka-1:29092 \
  --topic test --from-beginning \
  --consumer-property client.rack=rack-1
```

# Unclean leader election example
1. Create topic "test" with replication-factor 2
    ```shell
    export KAFKA_OPTS= && export KAFKA_JMX_OPTS=
    ```
    ```shell
    kafka-topics --create --topic test --replica-assignment 2:3 --bootstrap-server kafka-1:29092
    ```
2. Produce to topic
    ```shell
    kafka-console-producer --topic test --bootstrap-server kafka-1:29092
    ```
3. Stop follower
4. Produce to topic
     ```shell
    kafka-console-producer --topic test --bootstrap-server kafka-1:29092
    ```
5. Stop leader
6. Start follower
#### Default behavior: new leader isn't elected
#### With unclean.leader.election.enable=true: new leader is elected, messages are lost

# Clean up
```shell
docker-compose down
docker volume prune -f
```
