# Search query optimization analysis

Analysis of the biosamples-search query-building logic for further optimization opportunities. **No code changes** — recommendations only.

---

## 1. Already done

- **Public + release date range:** When both `pub` and a release `dt` filter are present, the query now uses a single release range and one `must_not` (exclude suppressed) instead of two ranges and extra nesting. Implemented in `QueryHelper` + `PublicSearchFilter.getExcludeSuppressedQuery()`.

---

## 2. Recommended optimizations

### 2.1 Multiple date ranges on the same field

**Current behaviour:** If a request includes more than one `dt` filter on the same field (e.g. two release ranges), each becomes a separate range clause and ES applies both (intersection).

**Opportunity:** In `QueryHelper.getFilterQuery()`, before building the final bool:

- Collect all `DateRangeSearchFilter` instances and group by `(field)`.
- For each field with multiple ranges, compute one range: `gte = max(froms)`, `lte = min(tos)` (intersection).
- Add a single range query per field and add other filters as today.

**Benefit:** One range per field instead of N; simpler and slightly more efficient.  
**Effort:** Low–medium (grouping + intersection logic, then single `DateRangeSearchFilter` or a single range query per field).

---

### 2.2 Top-level query when search text is empty

**Current behaviour:** The top-level query is always:

```text
bool.must( match_query )  // match_all when text is empty
.filter( filter_query )
```

**Opportunity:** When `searchQuery.getText()` is null or blank, use only the filter as the root query (e.g. `bool.filter(filterQuery)` with no `must`), instead of `bool.must(match_all).filter(filterQuery)`.

**Benefit:** Slightly smaller query tree and one less clause; semantics unchanged.  
**Effort:** Low (branch in `QueryHelper.getSearchQuery()` on empty text).

---

### 2.3 AttributeSearchFilter with a single value

**Current behaviour:** For one value we still build:

```text
nested(path=characteristics, bool.must( term(key), bool.should( term(value) ) ))
```

**Opportunity:** When `values.size() == 1`, use a single `term` for the value instead of `bool.should([term])`.

**Benefit:** Slightly simpler inner bool; minor.  
**Effort:** Low (branch in `AttributeSearchFilter.buildSubQueryForOrCondition()` or equivalent).

---

## 3. Edge cases and robustness (no performance gain, but safer)

### 3.1 StructuredDataSearchFilter with no criteria

**Current behaviour:** If `dataType`, `key`, and `value` are all null/empty, `queries` is empty and we build a nested query with `bool.must([])`. In Elasticsearch, an empty `must` can lead to surprising matching behaviour.

**Recommendation:** Reject or ignore the filter when none of `dataType`, `key`, `value` have text (e.g. throw or skip adding this filter), or document that at least one is required. Comment in code already says “dataType is required” but it’s not enforced.

---

### 3.2 ExternalRefSearchFilter with no criteria

**Current behaviour:** If both `archive` and `accession` are null/empty, `queries` is empty and we build `nested(path=externalReferences, bool.must([]))`.

**Recommendation:** Same as 3.1 — reject or skip when both are empty, or document that at least one is required.

---

### 3.3 RelationshipSearchFilter with no criteria

**Current behaviour:** If `relType`, `source`, and `target` are all null/empty, we build a nested query with `bool.must([])`.

**Recommendation:** Reject or skip when all three are empty; or require at least one.

---

## 4. Lower priority / optional

### 4.1 PublicSearchFilter + multiple release date ranges

If a client ever sends **pub** plus **two** release `dt` filters, we already skip the pub “1970→now” range (because `hasReleaseDateRange` is true), but we still add two separate range clauses. Merging date ranges on the same field (see 2.1) would automatically turn that into one range (intersection) and keep the current pub optimization behaviour.

### 4.2 Filter ordering

Elasticsearch does not guarantee evaluation order for filter clauses. Putting the most selective filters first is sometimes suggested for caching or early termination, but it’s index-dependent and not a general win. Leave as-is unless profiling shows a clear benefit.

---

## 5. Summary table

| Item                               | Benefit        | Effort   | Recommendation        |
|------------------------------------|----------------|----------|------------------------|
| Multiple date ranges, same field   | Clear          | Low–Med  | Implement              |
| No must(match_all) when text empty | Small          | Low      | Implement              |
| Single-value AttributeSearchFilter| Minor          | Low      | Optional               |
| Empty StructuredDataSearchFilter   | Correctness    | Low      | Validate / reject      |
| Empty ExternalRefSearchFilter      | Correctness    | Low      | Validate / reject      |
| Empty RelationshipSearchFilter    | Correctness    | Low      | Validate / reject      |

---

## 6. Files involved

- **Query building:** `QueryHelper.java`, `SearchService.java`, `FacetService.java`
- **Filters:** All under `server/.../filter/` (e.g. `DateRangeSearchFilter`, `PublicSearchFilter`, `AttributeSearchFilter`, `StructuredDataSearchFilter`, `ExternalRefSearchFilter`, `RelationshipSearchFilter`)

No changes have been made in the codebase; this document is analysis and recommendations only.
