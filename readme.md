
write samples to kafka (from db, from create call)
read samples from kafka and index in elastic search (keep last consumed index)
samples search endpoint read from elastic search


### Elastic
```shell
curl -u elastic:elastic -X GET "http://localhost:9200/_cluster/health?pretty"
```

### Kafka
```shell
docker exec -it kafka kafka-topics --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092

docker exec -it kafka kafka-console-producer --broker-list localhost:9092 --topic test-topic

docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic --from-beginning

```

### Gradle
```shell
./gradlew build
./gradlew build -x test
```

