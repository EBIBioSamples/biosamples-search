# Postman queries: investigate `filter=acc:SAME.*` at Elasticsearch level

Use these against your Elasticsearch instance. Default from `application.yml`: **base URL** `http://localhost:9200`, and if auth is enabled use **Basic Auth** (e.g. `elastic` / `elastic`).

---

## 1. Cluster health (connectivity)

**Method:** `GET`  
**URL:** `http://localhost:9200/`

**Purpose:** Confirm ES is reachable.

---

## 2. Index exists and doc count

**Method:** `GET`  
**URL:** `http://localhost:9200/samples/_count`

**Purpose:** Confirm `samples` index exists and has documents.

---

## 3. Index mapping (how `accession` is stored)

**Method:** `GET`  
**URL:** `http://localhost:9200/samples/_mapping`

**Purpose:** Check whether you have:
- `accession` as **keyword** (no `.keyword` subfield), or  
- `accession` as **text** with subfield `accession.keyword`.

Our code queries `accession.keyword`; if the mapping only has `accession` (keyword), wildcard must target `accession`, not `accession.keyword`.

---

## 4. Sample documents (see real accession values)

**Method:** `POST`  
**URL:** `http://localhost:9200/samples/_search`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "size": 5,
  "_source": ["accession", "name", "update"],
  "query": { "match_all": {} }
}
```

**Purpose:** See actual accession values (e.g. SAMEA, SAME, SAMN, SAMD). Check if any start with `SAME` for the pattern `SAME.*`.

---

## 5. Wildcard query on `accession.keyword` (what the app sends)

**Method:** `POST`  
**URL:** `http://localhost:9200/samples/_search`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "size": 20,
  "_source": ["accession", "name"],
  "query": {
    "wildcard": {
      "accession.keyword": {
        "value": "SAME.*"
      }
    }
  }
}
```

**Purpose:** Replicate the app’s wildcard filter. If this returns 0 hits but step 6 (on `accession`) returns hits, the index uses `accession` (keyword) and the app should query that field.

---

## 6. Wildcard query on `accession` (if mapping has no `.keyword`)

**Method:** `POST`  
**URL:** `http://localhost:9200/samples/_search`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "size": 20,
  "_source": ["accession", "name"],
  "query": {
    "wildcard": {
      "accession": {
        "value": "SAME.*"
      }
    }
  }
}
```

**Purpose:** If step 5 returns nothing but the mapping (step 3) shows `accession` as keyword (no `accession.keyword`), this checks whether wildcard on `accession` works and has data.

---

## 7. Full app-like query (bool + wildcard + public filter)

The app wraps the accession filter in a bool with other filters (e.g. release date, must_not suppressed). To test with only the wildcard (no date/status):

**Method:** `POST`  
**URL:** `http://localhost:9200/samples/_search`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):**

```json
{
  "size": 20,
  "_source": ["accession", "name", "release", "status"],
  "query": {
    "bool": {
      "must": [
        {
          "wildcard": {
            "accession.keyword": { "value": "SAME.*" }
          }
        }
      ]
    }
  }
}
```

If you use the same bool structure as the app (with `must_not` for suppressed, etc.), add those clauses here to see if they remove all hits.

---

## 8. Term query (exact accession) – sanity check

**Method:** `POST`  
**URL:** `http://localhost:9200/samples/_search`

**Headers:** `Content-Type: application/json`

**Body (raw JSON):** Replace `SAMD00000001` with a real accession from step 4.

```json
{
  "size": 5,
  "_source": ["accession", "name"],
  "query": {
    "term": {
      "accession.keyword": { "value": "SAMD00000001" }
    }
  }
}
```

**Purpose:** If term on `accession.keyword` works, the field exists. If it fails, try `"accession": { "value": "SAMD00000001" }` (no `.keyword`).

---

## Summary

| Step | What to check |
|------|----------------|
| 3    | Mapping: `accession` vs `accession.keyword` |
| 4    | Real accessions: any starting with `SAME`? |
| 5    | Wildcard on `accession.keyword` (app behaviour) |
| 6    | Wildcard on `accession` if mapping has no `.keyword` |
| 8    | Term on `accession.keyword` or `accession` to confirm field name |

If step 5 returns 0 and step 6 returns hits, the fix is to use field `accession` instead of `accession.keyword` in `AccessionSearchFilter` when your index mapping has no `.keyword` subfield.

---

## When `filter=acc:SAME*` returns nothing (API)

1. **URL encoding** – Some clients or proxies drop `*` in the query string. Try with the asterisk encoded:  
   `http://localhost:8081/biosamples/samples?filter=acc:SAME%2A`

2. **Other filters** – The app always adds a “public” filter: `release <= now` and `must_not` INSDC status = suppressed. If all documents matching `SAME*` are suppressed or not yet released, the API returns 0. Replicate the full query in ES (step 7 below) to confirm.

3. **Full app query in ES** – Run this to see if the combination of wildcard + release + not suppressed returns any hits (replace the date with current ISO instant if needed):

**POST** `http://localhost:9200/samples/_search`

```json
{
  "size": 20,
  "_source": ["accession", "name", "release", "characteristics"],
  "query": {
    "bool": {
      "must": [
        { "wildcard": { "accession.keyword": { "value": "SAME*" } } },
        { "range": { "release": { "lte": "now" } } }
      ],
      "must_not": [
        { "nested": { "path": "characteristics", "query": { "bool": { "must": [
          { "term": { "characteristics.key.keyword": "INSDC status" } },
          { "term": { "characteristics.value.keyword": "suppressed" } }
        ]}}}}
      ]
    }
  }
}
```

If this returns 0 hits, the 43 SAME* documents are all filtered out by release date or suppressed status.
