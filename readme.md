# BioSamples Search
BioSamples search is a Spring Boot application leveraging ElasticSearch for full-text search, filtering and faceting.

## Project Structure
The project contains 2 modules: `proto` and `server`. The following list shows important directories in the project.
+ `proto` - protobuf definitions and generated code
+ `server` - server application exposing search endpoints
  ..
    - `model` - core biosamples model
    - `filter` - filtering related code
    - `facet` - faceting related code
+ `helm` - cicd, k8s deployment
+ `k8s` - other related deployment files (ES, PV, ..)
+ `docs` - further documentation


## API
Three main APIs are exposed by the application.
1. Search samples (POST, GRPC)
2. Search samples streaming (GRPC)
3. Get facets for search (POST, GRPC)

BioSamples core services uses GRPC to communicate with `biosamples-search`. The RESTfull services are implemented mainly for the testing and development purposes. 

### Build

#### Requirements
- Java 24

```shell
./gradlew build
# build without unit and integration tests
./gradlew build -x test -x check
# build only proto module
./gradlew :proto:build
```

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
    "sort": [
      {
        "direction": "DESC",
        "field": "create"
      }
    ]
}'
```

### Get facets for search
#### POST
```shell
curl --location 'http://localhost:8080/facet' \
--header 'Content-Type: application/json' \
--data '{
    "text": "live",
    "filters": [
    ]
}'
```


### Facets
Currently, there are two faceting strategies implemented. The default implementation `RegularFacetingStratey` could be slow due to large number of attributes in BioSamples database. 
The `SamplingFacetingStrategy` uses sampling method to get facets from all shards faster, but is not providing the exact facet count. 
It is possible to limit the set of attributes to be faceted for even faster results. This is left as a future enhancement. 

