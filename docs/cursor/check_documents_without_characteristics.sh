#!/bin/bash

# Check if there are documents without the characteristics field
# Replace with your Elasticsearch credentials and index name

ELASTICSEARCH_URL="http://localhost:9200"
INDEX_NAME="samples"
USERNAME="elastic"
PASSWORD="elastic"

echo "=== Checking for documents WITHOUT characteristics field ==="
echo ""

# Query 1: Count documents that don't have characteristics field
curl -u "${USERNAME}:${PASSWORD}" -X POST "${ELASTICSEARCH_URL}/${INDEX_NAME}/_search?pretty" \
-H 'Content-Type: application/json' \
-d '{
  "size": 0,
  "query": {
    "bool": {
      "must_not": [
        {
          "exists": {
            "field": "characteristics"
          }
        }
      ]
    }
  },
  "aggs": {
    "total_without_characteristics": {
      "value_count": {
        "field": "_id"
      }
    }
  }
}'

echo ""
echo ""
echo "=== Checking for documents WITH characteristics field ==="
echo ""

# Query 2: Count documents that have characteristics field
curl -u "${USERNAME}:${PASSWORD}" -X POST "${ELASTICSEARCH_URL}/${INDEX_NAME}/_search?pretty" \
-H 'Content-Type: application/json' \
-d '{
  "size": 0,
  "query": {
    "exists": {
      "field": "characteristics"
    }
  },
  "aggs": {
    "total_with_characteristics": {
      "value_count": {
        "field": "_id"
      }
    }
  }
}'

echo ""
echo ""
echo "=== Sample documents without characteristics (first 5) ==="
echo ""

# Query 3: Get sample documents without characteristics
curl -u "${USERNAME}:${PASSWORD}" -X POST "${ELASTICSEARCH_URL}/${INDEX_NAME}/_search?pretty" \
-H 'Content-Type: application/json' \
-d '{
  "size": 5,
  "_source": ["accession", "name"],
  "query": {
    "bool": {
      "must_not": [
        {
          "exists": {
            "field": "characteristics"
          }
        }
      ]
    }
  }
}'

echo ""
echo ""
echo "=== Total document count ==="
echo ""

# Query 4: Total document count
curl -u "${USERNAME}:${PASSWORD}" -X GET "${ELASTICSEARCH_URL}/${INDEX_NAME}/_count?pretty"



