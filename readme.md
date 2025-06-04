# BioSamples Search
BioSamples search is a Spring Boot application leveraging ElasticSearch for full text search, filtering and faceting.

## Project Structure
The project contains 2 modules: `proto` and `server`. 
- `proto` - Protobuf definitions and generated code
- `server` - Spring Boot application exposing endpoints

## API
We are supporting 3 main APIs
1. Search samples (POST, GRPC)
2. Search samples streaming (GRPC)
3. Get facets for search (POST, GRPC)



### Search samples
#### POST
```shell
curl --location 'http://localhost:8080/search' \
--header 'Content-Type: application/json' \
--data '{
    "text": "soil",
    "filters": [
      {
        "type": "attr",
        "field": "env_medium",
        "values": ["Soil"]
      },
      {
        "type": "attr",
        "field": "locus_tag_prefix",
        "values": ["SM2"]
      },
      {
        "type": "acc",
        "accession": "SAMD00000364"
      },
      {
        "type": "dt",
        "field": "create",
        "from": "2014-04-21T00:00:00Z",
        "to": "2014-04-22T05:00:00Z"
      }
    ],
    "page": 0,
    "size": 3,
    "sort": []
}'
```

#### GRPC
```shell

```

### Search samples streaming
#### GRPC
```shell

```

### Get facets for search
#### POST
```shell
curl --location 'http://localhost:8080/facet' \
--header 'Content-Type: application/json' \
--data '{
    "text": "live",
    "filters": [
    ],
    "cursor": "",
    "page": 0,
    "size": 3,
    "sort": []
}'
```

#### GRPC
```shell

```




## Just some notes

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

