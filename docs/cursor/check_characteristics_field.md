# Checking for Documents Without Characteristics Field

## Quick Check Commands

### 1. Count documents WITHOUT characteristics field

```bash
curl -u elastic:elastic -X POST "http://localhost:9200/samples/_search?pretty" \
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
```

### 2. Count documents WITH characteristics field

```bash
curl -u elastic:elastic -X POST "http://localhost:9200/samples/_search?pretty" \
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
```

### 3. Get sample documents without characteristics (first 5)

```bash
curl -u elastic:elastic -X POST "http://localhost:9200/samples/_search?pretty" \
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
```

### 4. Check if characteristics field is empty array

```bash
curl -u elastic:elastic -X POST "http://localhost:9200/samples/_search?pretty" \
-H 'Content-Type: application/json' \
-d '{
  "size": 5,
  "_source": ["accession", "characteristics"],
  "query": {
    "script": {
      "script": {
        "source": "doc[\"characteristics\"].size() == 0",
        "lang": "painless"
      }
    }
  }
}'
```

### 5. Check mapping for characteristics field

```bash
curl -u elastic:elastic -X GET "http://localhost:9200/samples/_mapping/field/characteristics?pretty"
```

## Understanding the Results

- **Documents without characteristics**: These might cause issues with nested queries if the query doesn't handle missing fields properly
- **Empty characteristics arrays**: Some documents might have `characteristics: []` which is different from missing field
- **Nested query behavior**: Nested queries in `must_not` should handle missing fields, but there might be edge cases

## Potential Issues

If you find documents without `characteristics`:
1. The nested query in `must_not` should still work (it will match documents without the field)
2. However, if there's a query syntax issue, it might fail
3. Check Elasticsearch version compatibility with nested queries in `must_not`



