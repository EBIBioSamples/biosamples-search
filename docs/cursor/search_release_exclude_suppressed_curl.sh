#!/bin/bash

# Curl command with explicit release date range AND exclude suppressed samples
# This breaks down the "pub" filter into explicit filters for debugging

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
        },
        {
            "type": "excludeAttr",
            "field": "INSDC status",
            "values": ["suppressed"]
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

