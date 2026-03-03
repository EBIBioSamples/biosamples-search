# Query the biosamples-search REST API to see what it returns.
# Use the port where the search service is running (default 8080; core app is usually 8081).
$SearchPort = if ($env:SEARCH_PORT) { $env:SEARCH_PORT } else { "8080" }
$BaseUrl = "http://localhost:$SearchPort/search"

Write-Host "=== 1. Exact accession (acc:SAMEA26) ==="
$exactBody = @'
{"text":null,"filters":[{"type":"pub","webinId":""},{"type":"acc","accession":"SAMEA26"}],"facets":null,"page":0,"size":20,"sort":null,"searchAfter":null}
'@
Invoke-RestMethod -Uri $BaseUrl -Method Post -ContentType "application/json" -Body $exactBody | ConvertTo-Json -Depth 10

Write-Host ""
Write-Host "=== 2. Wildcard accession (acc:SAME*) ==="
$wildcardBody = @'
{"text":null,"filters":[{"type":"pub","webinId":""},{"type":"acc","accession":"SAME*"}],"facets":null,"page":0,"size":20,"sort":null,"searchAfter":null}
'@
Invoke-RestMethod -Uri $BaseUrl -Method Post -ContentType "application/json" -Body $wildcardBody | ConvertTo-Json -Depth 10
