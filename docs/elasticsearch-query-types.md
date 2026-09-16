# Elasticsearch Query Types In This Project

This note explains:

1. which Elasticsearch query types this project actually uses
2. how the project composes them
3. what other query types Elasticsearch supports
4. JSON examples for each category

## 1. Query Types Used In This Project

The active search code is centered on [`QueryHelper`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:19) and the filter implementations under [`server/src/main/java/uk/ac/ebi/biosamples/search/filter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter).

The project uses these query types:

- `bool`
- `match_all`
- `match_phrase`
- `query_string`
- `term`
- `wildcard`
- `range` for dates
- `nested`

## 2. How `bool` Is Used Here

`bool` is the main query container in this project.

Reference: [`QueryHelper.getSearchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:19)

The service combines:

- `must` for the text query
- `filter` for structured filters
- `must_not` for exclusions like suppressed samples
- `should` for alternative text matching and public-or-authenticated access logic

### Example from this project's shape

```json
{
  "bool": {
    "must": [
      {
        "bool": {
          "should": [
            {
              "match_phrase": {
                "sample_full_text": {
                  "query": "human liver",
                  "boost": 5.0
                }
              }
            },
            {
              "query_string": {
                "default_field": "sample_full_text",
                "query": "human liver",
                "default_operator": "AND"
              }
            }
          ],
          "minimum_should_match": 1
        }
      }
    ],
    "filter": [
      {
        "term": {
          "domain.keyword": "self.BioSamples"
        }
      }
    ]
  }
}
```

## 3. `match_all`

Used when there is no text query and no structured filter.

References:

- [`QueryHelper.getSearchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:21)
- [`QueryHelper.getFilterQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:70)

### Example

```json
{
  "match_all": {}
}
```

## 4. `match_phrase`

Used for:

- quoted text searches
- boosted phrase preference for unquoted text

Reference: [`QueryHelper.getTextMatchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:38)

### Example

```json
{
  "match_phrase": {
    "sample_full_text": {
      "query": "human liver"
    }
  }
}
```

### Boosted example

```json
{
  "match_phrase": {
    "sample_full_text": {
      "query": "human liver",
      "boost": 5.0
    }
  }
}
```

## 5. `query_string`

Used for unquoted text search, with `AND` semantics across terms.

Reference: [`QueryHelper.getTextMatchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:55)

### Example

```json
{
  "query_string": {
    "default_field": "sample_full_text",
    "query": "human liver",
    "default_operator": "AND"
  }
}
```

## 6. `term`

Used for exact matching on `.keyword` fields and exact nested-field comparisons.

Examples in code:

- [`DomainSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/DomainSearchFilter.java:8)
- [`NameSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/NameSearchFilter.java:8)
- [`WebinIdSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/WebinIdSearchFilter.java:8)

### Example

```json
{
  "term": {
    "domain.keyword": "self.BioSamples"
  }
}
```

### Nested exact-match example

```json
{
  "term": {
    "characteristics.key.keyword": "organism"
  }
}
```

## 7. `wildcard`

Used only for accession-style wildcard searching in this project.

Reference: [`AccessionSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AccessionSearchFilter.java:12)

### Example

```json
{
  "wildcard": {
    "accession.keyword": {
      "value": "SAMEA*"
    }
  }
}
```

## 8. `range` For Dates

Used for `release`, `update`, `submitted`, and `create`.

Reference: [`DateRangeSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/DateRangeSearchFilter.java:18)

### Example

```json
{
  "range": {
    "release": {
      "gte": "2020-01-01T00:00:00Z",
      "lte": "2020-12-31T23:59:59Z"
    }
  }
}
```

### Project-style open-ended example

```json
{
  "range": {
    "update": {
      "gte": "1970-01-01T00:00:00.000Z",
      "lte": "now"
    }
  }
}
```

## 9. `nested`

Used whenever the query targets nested arrays of objects:

- `characteristics`
- `relationships`
- `externalReferences`
- `structuredData`

Examples in code:

- [`AttributeSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AttributeSearchFilter.java:14)
- [`RelationshipSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/RelationshipSearchFilter.java:39)
- [`ExternalRefSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/ExternalRefSearchFilter.java:31)
- [`StructuredDataSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/StructuredDataSearchFilter.java:40)

### Example: attribute filter

```json
{
  "nested": {
    "path": "characteristics",
    "query": {
      "bool": {
        "must": [
          {
            "term": {
              "characteristics.key.keyword": "organism"
            }
          },
          {
            "bool": {
              "should": [
                {
                  "term": {
                    "characteristics.value.keyword": "Homo sapiens"
                  }
                },
                {
                  "term": {
                    "characteristics.value.keyword": "Mus musculus"
                  }
                }
              ]
            }
          }
        ]
      }
    }
  }
}
```

### Example: external reference filter

```json
{
  "nested": {
    "path": "externalReferences",
    "query": {
      "bool": {
        "must": [
          {
            "term": {
              "externalReferences.archive.keyword": "ENA"
            }
          },
          {
            "term": {
              "externalReferences.accession.keyword": "ERR123456"
            }
          }
        ]
      }
    }
  }
}
```

## 10. Full Example In This Project's Style

This is the kind of combined query the service effectively builds.

```json
{
  "bool": {
    "must": [
      {
        "bool": {
          "should": [
            {
              "match_phrase": {
                "sample_full_text": {
                  "query": "human liver",
                  "boost": 5.0
                }
              }
            },
            {
              "query_string": {
                "default_field": "sample_full_text",
                "query": "human liver",
                "default_operator": "AND"
              }
            }
          ],
          "minimum_should_match": 1
        }
      }
    ],
    "filter": [
      {
        "term": {
          "domain.keyword": "self.BioSamples"
        }
      },
      {
        "nested": {
          "path": "characteristics",
          "query": {
            "bool": {
              "must": [
                {
                  "term": {
                    "characteristics.key.keyword": "organism"
                  }
                },
                {
                  "bool": {
                    "should": [
                      {
                        "term": {
                          "characteristics.value.keyword": "Homo sapiens"
                        }
                      }
                    ]
                  }
                }
              ]
            }
          }
        }
      }
    ]
  }
}
```

## 11. Other Elasticsearch Query Types Supported By ES

The project does not use all of these, but Elasticsearch supports them.

## 12. Full-Text Query Types

### `match`

Good for analyzed text search on one field.

```json
{
  "match": {
    "sample_full_text": "human liver"
  }
}
```

### `multi_match`

Good when searching several text fields together.

```json
{
  "multi_match": {
    "query": "human liver",
    "fields": ["name", "sample_full_text"]
  }
}
```

### `simple_query_string`

Safer alternative to `query_string` for end-user input.

```json
{
  "simple_query_string": {
    "query": "human liver +mouse",
    "fields": ["sample_full_text"]
  }
}
```

### `combined_fields`

Treat multiple fields like one combined analyzed field.

```json
{
  "combined_fields": {
    "query": "human liver",
    "fields": ["name", "sample_full_text"]
  }
}
```

### `intervals`

Fine-grained positional matching.

```json
{
  "intervals": {
    "sample_full_text": {
      "match": {
        "query": "human liver",
        "max_gaps": 2,
        "ordered": true
      }
    }
  }
}
```

## 13. Term-Level Query Types

### `terms`

Exact match against any of several values.

```json
{
  "terms": {
    "domain.keyword": ["self.BioSamples", "self.ENA"]
  }
}
```

### `prefix`

Efficient prefix matching on keyword-like fields.

```json
{
  "prefix": {
    "accession.keyword": "SAMEA"
  }
}
```

### `regexp`

Regular expression matching.

```json
{
  "regexp": {
    "accession.keyword": "SAMEA[0-9]+"
  }
}
```

### `fuzzy`

Approximate spelling match.

```json
{
  "fuzzy": {
    "name.keyword": {
      "value": "hommo sapiens",
      "fuzziness": "AUTO"
    }
  }
}
```

### `exists`

Find documents where a field is present.

```json
{
  "exists": {
    "field": "structuredData"
  }
}
```

### `ids`

Lookup by document ids.

```json
{
  "ids": {
    "values": ["SAMEA1", "SAMEA2"]
  }
}
```

### `terms_set`

Require overlap with a set of values.

```json
{
  "terms_set": {
    "tags.keyword": {
      "terms": ["a", "b", "c"],
      "minimum_should_match_script": {
        "source": "2"
      }
    }
  }
}
```

## 14. Compound Query Types

### `dis_max`

Pick the best-scoring clause.

```json
{
  "dis_max": {
    "queries": [
      {
        "match": {
          "name": "human liver"
        }
      },
      {
        "match": {
          "sample_full_text": "human liver"
        }
      }
    ]
  }
}
```

### `constant_score`

Run a filter but give every match the same score.

```json
{
  "constant_score": {
    "filter": {
      "term": {
        "domain.keyword": "self.BioSamples"
      }
    }
  }
}
```

### `function_score`

Adjust scores with functions.

```json
{
  "function_score": {
    "query": {
      "match": {
        "sample_full_text": "human liver"
      }
    },
    "functions": [
      {
        "weight": 2
      }
    ],
    "boost_mode": "multiply"
  }
}
```

### `boosting`

Penalize documents that match a negative query.

```json
{
  "boosting": {
    "positive": {
      "match": {
        "sample_full_text": "human liver"
      }
    },
    "negative": {
      "term": {
        "status.keyword": "suppressed"
      }
    },
    "negative_boost": 0.2
  }
}
```

## 15. Joining And Nested-Relation Query Types

### `has_child`

Parent-child model child query.

```json
{
  "has_child": {
    "type": "comment",
    "query": {
      "match": {
        "text": "important"
      }
    }
  }
}
```

### `has_parent`

Parent-child model parent query.

```json
{
  "has_parent": {
    "parent_type": "sample",
    "query": {
      "term": {
        "domain.keyword": "self.BioSamples"
      }
    }
  }
}
```

### `parent_id`

Find children of a specific parent.

```json
{
  "parent_id": {
    "type": "comment",
    "id": "SAMEA1"
  }
}
```

## 16. Geo Query Types

### `geo_distance`

```json
{
  "geo_distance": {
    "distance": "10km",
    "location": {
      "lat": 51.5,
      "lon": -0.12
    }
  }
}
```

### `geo_bounding_box`

```json
{
  "geo_bounding_box": {
    "location": {
      "top_left": {
        "lat": 52.0,
        "lon": -1.0
      },
      "bottom_right": {
        "lat": 51.0,
        "lon": 0.0
      }
    }
  }
}
```

### `geo_shape`

```json
{
  "geo_shape": {
    "location": {
      "shape": {
        "type": "envelope",
        "coordinates": [[-1.0, 52.0], [0.0, 51.0]]
      },
      "relation": "within"
    }
  }
}
```

## 17. Script And Advanced Scoring Query Types

### `script`

```json
{
  "script": {
    "script": {
      "source": "doc['taxId'].value > params.min",
      "params": {
        "min": 1000
      }
    }
  }
}
```

### `script_score`

```json
{
  "script_score": {
    "query": {
      "match": {
        "sample_full_text": "human liver"
      }
    },
    "script": {
      "source": "_score * 2"
    }
  }
}
```

### `distance_feature`

```json
{
  "distance_feature": {
    "field": "update",
    "origin": "now",
    "pivot": "30d"
  }
}
```

### `rank_feature`

```json
{
  "rank_feature": {
    "field": "popularity"
  }
}
```

## 18. Special-Purpose Query Types

### `percolate`

```json
{
  "percolate": {
    "field": "query",
    "document": {
      "name": "human liver sample"
    }
  }
}
```

### `pinned`

```json
{
  "pinned": {
    "ids": ["SAMEA1", "SAMEA2"],
    "organic": {
      "match": {
        "sample_full_text": "human liver"
      }
    }
  }
}
```

## 19. Span Query Types

Used for advanced positional text matching.

### `span_term`

```json
{
  "span_term": {
    "sample_full_text": "human"
  }
}
```

### `span_near`

```json
{
  "span_near": {
    "clauses": [
      {
        "span_term": {
          "sample_full_text": "human"
        }
      },
      {
        "span_term": {
          "sample_full_text": "liver"
        }
      }
    ],
    "slop": 2,
    "in_order": true
  }
}
```

### `span_or`

```json
{
  "span_or": {
    "clauses": [
      {
        "span_term": {
          "sample_full_text": "human"
        }
      },
      {
        "span_term": {
          "sample_full_text": "mouse"
        }
      }
    ]
  }
}
```

### `span_not`

```json
{
  "span_not": {
    "include": {
      "span_term": {
        "sample_full_text": "human"
      }
    },
    "exclude": {
      "span_term": {
        "sample_full_text": "mouse"
      }
    }
  }
}
```

## 20. Vector Search Query Type

Modern Elasticsearch can also do vector search.

### `knn`

```json
{
  "knn": {
    "field": "embedding",
    "query_vector": [0.12, 0.98, -0.42],
    "k": 10,
    "num_candidates": 100
  }
}
```

## 21. What This Project Does Not Currently Use

This codebase does not currently appear to use:

- `match`
- `multi_match`
- `simple_query_string`
- `combined_fields`
- `terms`
- `prefix`
- `regexp`
- `fuzzy`
- `exists`
- `dis_max`
- `constant_score`
- `function_score`
- geo queries
- parent/child queries
- script queries
- vector search queries

## 22. Practical Summary

For this project, the most important query types are:

1. `bool`
2. `match_phrase`
3. `query_string`
4. `term`
5. `range`
6. `nested`
7. `wildcard`

If you are tracing real application behavior, start from:

- [`QueryHelper`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:19)
- the filter classes in [`server/src/main/java/uk/ac/ebi/biosamples/search/filter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter)

That is where the actual Elasticsearch query tree is built.
