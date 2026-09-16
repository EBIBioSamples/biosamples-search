# Elasticsearch Features Used In This Project

This note walks through the Elasticsearch features used by the `biosamples-search` service in the order a request typically uses them.

## 1. Index Mapping Through Spring Data Annotations

The Elasticsearch document model is [`Sample`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/Sample.java:17).

Key mapping features:

- `@Document(indexName = "samples")`
- `@Field(type = FieldType.Date)` for `release`, `update`, `create`, `submitted`
- `@Field(type = FieldType.Nested)` for `characteristics`, `relationships`, `externalReferences`, `structuredData`, and others

Those nested mappings are what make the nested queries and nested aggregations in this project valid.

## 2. Repository-Based Indexing

Writes go through Spring Data Elasticsearch repository support in [`SamplesRepository`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SamplesRepository.java:7).

Incoming samples are queued and written in batches by [`IndexingListener`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/index/IndexingListener.java:22).

Relevant Elasticsearch behavior:

- `saveAll(samples)` is used for batch persistence
- the service writes application `Sample` documents, not raw JSON requests

## 3. Native Query Construction

Searches are built with Spring Data Elasticsearch `NativeQuery` and the Elasticsearch Java client DSL rather than raw JSON.

- Search path: [`SearchService`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SearchService.java:64)
- Facet path: [`FacetService`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/FacetService.java:51)

## 4. `bool` Query Composition

All request-level query composition starts in [`QueryHelper`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:19).

Main pattern:

- `must` for text search
- `filter` for structured filters
- `must_not` for exclusions where needed
- `should` for alternative matches

This is the core Elasticsearch query feature used across the project.

## 5. `match_all` Fallback

When no text or filters are provided, the service falls back to `match_all`.

References:

- [`QueryHelper.getSearchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:19)
- [`QueryHelper.getFilterQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:69)

## 6. Full-Text Search

For text search on `sample_full_text`, the project uses:

- `match_phrase` for quoted input
- `match_phrase` with a boost for exact phrase preference on unquoted input
- `query_string` with `defaultOperator(AND)` for broader term matching

Reference: [`QueryHelper.getTextMatchQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:31)

This gives the service a ranking preference for exact phrases while still allowing documents that contain all terms in any order.

## 7. Exact Match Filtering With `term`

Most scalar filters are implemented as `term` queries against `.keyword` fields.

Examples:

- accession: [`AccessionSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AccessionSearchFilter.java:19)
- SRA accession: [`SraAccessionSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/SraAccessionSearchFilter.java:8)
- name: [`NameSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/NameSearchFilter.java:8)
- domain: [`DomainSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/DomainSearchFilter.java:8)
- webin id: [`WebinIdSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/WebinIdSearchFilter.java:8)

## 8. Wildcard Filtering

Accession filtering can switch to Elasticsearch `wildcard` query mode when the input contains wildcard characters.

Reference: [`AccessionSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AccessionSearchFilter.java:12)

Notable behavior:

- runs on `accession.keyword`
- converts `.*` to `*`
- uses exact `term` when the pattern is not a wildcard

## 9. Date Range Queries

Date filters use Elasticsearch date range query support over:

- `release`
- `update`
- `submitted`
- `create`

Reference: [`DateRangeSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/DateRangeSearchFilter.java:18)

Behavior:

- `gte(from)` if provided, otherwise `1970-01-01T00:00:00.000Z`
- `lte(to)` if provided, otherwise `now`
- special handling for a far-future sentinel value

## 10. Date Range Merging

If multiple date filters target the same field, the service intersects them before sending the query to Elasticsearch.

Reference: [`QueryHelper.getMergedDateRangeQueries`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/es/QueryHelper.java:113)

The merge rule is:

- latest `from`
- earliest `to`

That reduces redundant range clauses and keeps the effective filter precise.

## 11. Nested Queries

Because several arrays are mapped as `nested`, the service uses Elasticsearch `nested` queries to preserve object boundaries.

Examples:

- `characteristics`: [`AttributeSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AttributeSearchFilter.java:14)
- `relationships`: [`RelationshipSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/RelationshipSearchFilter.java:39)
- `externalReferences`: [`ExternalRefSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/ExternalRefSearchFilter.java:31)
- `structuredData`: [`StructuredDataSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/StructuredDataSearchFilter.java:40)

This matters because it prevents cross-object field matching inside arrays of objects.

## 12. Boolean Logic Inside Nested Queries

Inside nested queries the project uses:

- `must` to require multiple fields from the same nested object
- `should` for one-of-many value matching

Example: [`AttributeSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/AttributeSearchFilter.java:17) requires:

- `characteristics.key.keyword = field`
- one of the provided `characteristics.value.keyword` values

## 13. Exclusion With `must_not`

The public filter excludes suppressed records with `must_not`.

Reference: [`PublicSearchFilter`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/filter/PublicSearchFilter.java:14)

It also combines:

- a release-date constraint
- a suppressed-status exclusion
- optional authenticated access via `should`

## 14. Sorting

The service sorts using Elasticsearch sort support through Spring Data `Sort.Order`.

Reference: [`SearchService.getSortOrders`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SearchService.java:53)

Default sort order:

- `update` descending
- `accession.keyword` ascending

This stable sort order is also important for `search_after`.

## 15. Page-Based Pagination

For ordinary search requests, the service uses pageable search via `PageRequest`.

Reference: [`SearchService.getPage`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SearchService.java:46)

This is standard Spring Data Elasticsearch paging over an ES query.

## 16. Deep Pagination With `search_after`

For streaming and deep traversal, the service uses Elasticsearch `search_after`.

Reference: [`SearchService.search`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SearchService.java:33)

Behavior:

- if a `SearchAfter` token is present, the query gets `search_after`
- the token is composed from `update` and `accession`
- paging resets to page `0` when `search_after` is used

This is the project’s main deep-pagination feature.

## 17. `track_total_hits`

Search queries enable total hit tracking so the service can return page metadata.

Reference: [`SearchService.getEsNativeQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/samples/SearchService.java:64)

This supports fields like:

- `totalElements`
- `totalPages`

## 18. Aggregation-Only Queries

Facet requests use Elasticsearch as an aggregation engine by setting:

- `withMaxResults(0)`
- aggregation definitions
- a timeout

Reference: [`FacetService.getEsNativeQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/FacetService.java:51)

This means the facet path does not fetch normal search hits unless needed by Elasticsearch internals.

## 19. Nested Aggregations

Facet generation uses Elasticsearch nested aggregations on nested fields.

References:

- characteristics: [`AttributeFacet.getAggregations`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/AttributeFacet.java:25)
- relationships: [`RelationshipFacet.getAggregations`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/RelationshipFacet.java:16)
- external references: [`ExternalRefFacet.getAggregations`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/ExternalRefFacet.java:16)

## 20. `terms` Aggregations

Most facets are implemented with Elasticsearch `terms` aggregations over `.keyword` fields.

Examples:

- `characteristics.key.keyword`
- `characteristics.value.keyword`
- `relationships.type.keyword`
- `relationships.source.keyword`
- `relationships.target.keyword`
- `externalReferences.archive.keyword`

These are used to produce bucketed counts for UI/API facet responses.

## 21. Included and Excluded Terms In Aggregations

Attribute facets use both inclusion and exclusion lists:

- exclude noisy attribute keys in default faceting mode
- include only requested keys in targeted facet mode

Reference: [`AttributeFacet`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/AttributeFacet.java:15)

This is an important ES feature in the project because it controls facet cost and noise.

## 22. Date Histogram Aggregation

The update-date facet uses a `date_histogram` aggregation with yearly calendar intervals.

Reference: [`DateRangeFacet.getAggregations`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/DateRangeFacet.java:13)

Configuration:

- field: `update`
- interval: `CalendarInterval.Year`
- format: `yyyy-MM-dd`

There is commented-out code for `auto_date_histogram`, but it is not active.

## 23. Sampler Aggregation

The active default faceting strategy is sampling-based faceting.

References:

- strategy injection: [`FacetService`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/FacetService.java:28)
- implementation: [`SamplingFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/SamplingFacetingStrategy.java:29)

Feature used:

- Elasticsearch `sampler` aggregation with `shardSize(100000)`

Purpose:

- limit facet computation to a sample per shard
- reduce facet cost for large result sets
- estimate counts instead of computing every facet exactly

## 24. `value_count` Aggregation

The sampling strategy also uses `value_count` on `accession.keyword`.

Reference: [`SamplingFacetingStrategy.getDefaultAggregations`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/SamplingFacetingStrategy.java:34)

It is used in two places:

- top-level total document count
- sampled document count inside the sampler

Those values are then used to compute an extrapolation factor.

## 25. Aggregation Result Parsing

Facet strategies parse `SearchHits` aggregations through Spring Data wrappers:

- `ElasticsearchAggregations`
- `ElasticsearchAggregation`

References:

- [`RegularFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/RegularFacetingStrategy.java:29)
- [`SamplingFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/SamplingFacetingStrategy.java:95)

This is how raw ES aggregation results become application facet objects.

## 26. Aggregation Timeout

Facet queries set a timeout of 60 seconds.

Reference: [`FacetService.getEsNativeQuery`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/FacetService.java:51)

## 27. Multiple Faceting Strategies

The codebase contains multiple faceting strategies:

- [`RegularFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/RegularFacetingStrategy.java:18)
- [`SamplingFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/SamplingFacetingStrategy.java:19)
- [`ParallelFetchingFacetingStrategy`](C:/Users/dgupta/biosamples-peripheral-services/biosamples-search/server/src/main/java/uk/ac/ebi/biosamples/search/facet/ParallelFetchingFacetingStrategy.java:18)

At the moment, `FacetService` is wired to the sampling strategy.

## 28. Summary

The Elasticsearch feature set used in this project is centered on two modes:

1. document search:
   - full-text search
   - exact filters
   - nested filters
   - sorting
   - pagination and `search_after`

2. aggregation/faceting:
   - nested aggregations
   - `terms`
   - `date_histogram`
   - `sampler`
   - `value_count`

That is the core Elasticsearch surface area of this service.
