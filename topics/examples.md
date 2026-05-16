# Start docker compose
```shell
docker-compose up -d
```

# Write to topic:
```shell
docker exec -it kafka-course-topics-kafka-1 kafka-console-producer --topic test --bootstrap-server kafka:29092
```

# Read from topic:
```shell
docker exec -it kafka-course-topics-kafka-1 kafka-console-consumer --topic test --from-beginning --bootstrap-server kafka:29092
```

# Clean up
```shell
docker-compose down
docker volume prune -f
```
