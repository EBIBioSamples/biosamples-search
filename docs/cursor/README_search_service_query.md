# Querying the search service directly

The **biosamples-search** service exposes a REST API for development/testing (core app uses gRPC). Use it to see what the search service returns without going through the core app.

- **Search service** usually runs on **port 8080** (or set `SEARCH_PORT`).
- **Core app** (biosamples-v4) runs on **port 8081** and calls the search service via gRPC.

## Endpoint

- **POST** `http://localhost:8080/search`  
  Body: JSON `SearchQuery` (see examples below).

## Quick test (PowerShell)

From the repo root:

```powershell
# Exact accession
Invoke-RestMethod -Uri "http://localhost:8080/search" -Method Post -ContentType "application/json" -Body '{"filters":[{"type":"pub","webinId":""},{"type":"acc","accession":"SAMEA26"}],"page":0,"size":20}'

# Wildcard accession (asterisk in JSON is not URL-encoded)
Invoke-RestMethod -Uri "http://localhost:8080/search" -Method Post -ContentType "application/json" -Body '{"filters":[{"type":"pub","webinId":""},{"type":"acc","accession":"SAME*"}],"page":0,"size":20}'
```

## Scripts

- **Bash:** `./docs/cursor/query_search_service.sh` (from project root; needs `jq` for pretty output).
- **PowerShell:** `.\docs\cursor\query_search_service.ps1`

Override port: `$env:SEARCH_PORT=9090; .\docs\cursor\query_search_service.ps1`

## Request JSON format

Filters use a `type` discriminator and type-specific fields:

| type | Example |
|------|--------|
| `pub` | `{"type":"pub","webinId":""}` |
| `acc` | `{"type":"acc","accession":"SAME*"}` or `{"type":"acc","accession":"SAMEA26"}` |

Full query: `{"text":null,"filters":[...],"facets":null,"page":0,"size":20,"sort":null,"searchAfter":null}`

Pre-made bodies: `search_service_query_acc_exact.json`, `search_service_query_acc_wildcard.json`.

## How to interpret results

- If **exact** returns hits and **wildcard** returns hits → search service and ES are fine; the core app or URL handling is likely dropping `*` for `filter=acc:SAME*`.
- If **exact** returns hits and **wildcard** returns 0 → issue is inside the search service (e.g. wildcard query building).
- If both return 0 → check that the search service is pointing at the same ES index and that data exists (e.g. public, not suppressed).
