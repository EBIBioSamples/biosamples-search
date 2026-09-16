#!/bin/bash

# Curl command with release date range AND suppressed exclusion
# Uses "pub" filter which internally:
# 1. Filters by release date (release <= current time)
# 2. Excludes suppressed samples (INSDC status != "suppressed")

curl --location 'http://localhost:8080/search' \
--header 'Content-Type: application/json' \
--data '{
    "text": "",
    "filters": [
        {
            "type": "pub"
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

