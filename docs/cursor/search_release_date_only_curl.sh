#!/bin/bash

# Curl command with explicit release date range filter only
# Use this to test the release date filtering separately from suppressed exclusion

curl --location 'http://localhost:8080/search' \
--header 'Content-Type: application/json' \
--data '{
    "text": "",
    "filters": [
        {
            "type": "dt",
            "field": "release",
            "from": "1970-01-01T00:00:00.000Z",
            "to": "2025-12-15T23:59:59.999Z"
        }
    ],
    "page": 0,
    "size": 10,
    "sort": [
        {
            "direction": "DESC",
            "field": "update"
        }
    ]
}'



