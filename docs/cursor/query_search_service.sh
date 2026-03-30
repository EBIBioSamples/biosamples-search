#!/usr/bin/env bash
# Query the biosamples-search REST API to see what it returns.
# Use the port where the search service is running (default 8080; core app is usually 8081).
SEARCH_PORT="${SEARCH_PORT:-8080}"

echo "=== 1. Exact accession (acc:SAMEA26) ==="
curl -s -X POST "http://localhost:${SEARCH_PORT}/search" \
  -H "Content-Type: application/json" \
  -d @docs/cursor/search_service_query_acc_exact.json | jq .

echo ""
echo "=== 2. Wildcard accession (acc:SAME*) ==="
curl -s -X POST "http://localhost:${SEARCH_PORT}/search" \
  -H "Content-Type: application/json" \
  -d @docs/cursor/search_service_query_acc_wildcard.json | jq .
