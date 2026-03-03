# Faceting Aggregations Explained

## Overview

This document explains how Elasticsearch aggregations work for faceting in the BioSamples Search service, specifically covering:
- **Characteristics** (key-value attributes like organism, sex, geographic location)
- **Relationships** (sample relationships like "derived from", "parent of")
- **External References** (links to external archives like ENA, SRA)

## Data Structure

Each `Sample` document in Elasticsearch contains three nested fields that are used for faceting:

### 1. Characteristics (Nested Field)

A set of key-value pairs representing sample attributes:

```json
{
  "characteristics": [
    {"key": "organism", "value": "Homo sapiens"},
    {"key": "sex", "value": "male"},
    {"key": "geographic location", "value": "USA"},
    {"key": "env_medium", "value": "soil"}
  ]
}
```

**Model**: `Attribute` class with `key` (String) and `value` (String) fields.

**Common Keys**:
- `organism` - Species name
- `sex` - Gender/sex
- `geographic location` - Location information
- `env_medium` - Environmental medium
- And many more dynamic attributes

### 2. Relationships (Nested Field)

A set of relationships between samples:

```json
{
  "relationships": [
    {
      "type": "derived from",
      "source": "SAMD00000001",
      "target": "SAMD00000002"
    },
    {
      "type": "parent of",
      "source": "SAMD00000001",
      "target": "SAMD00000003"
    }
  ]
}
```

**Model**: `Relationship` record with `type`, `source`, and `target` fields.

**Common Types**:
- `derived from` - Sample was derived from another
- `parent of` - Sample is a parent of another
- `child of` - Sample is a child of another

### 3. External References (Nested Field)

A set of links to external archives and databases:

```json
{
  "externalReferences": [
    {
      "archive": "ENA",
      "accession": "SRR123456",
      "url": "https://www.ebi.ac.uk/ena/browser/view/SRR123456"
    },
    {
      "archive": "SRA",
      "accession": "SRS789012",
      "url": "https://..."
    }
  ]
}
```

**Model**: `ExternalReference` record with `archive`, `accession`, `url`, and `duo` fields.

**Common Archives**:
- `ENA` - European Nucleotide Archive
- `SRA` - Sequence Read Archive
- `GEO` - Gene Expression Omnibus

## Why Nested Aggregations?

All three fields are stored as **nested fields** in Elasticsearch. This means:

1. **Nested fields are stored as separate internal documents** - Elasticsearch treats each array element as a separate document internally
2. **Nested aggregations are required** - To properly aggregate nested data, you must use a `nested` aggregation that:
   - Enters the nested document context
   - Runs aggregations on nested documents
   - Returns results in the nested context

Without nested aggregation, Elasticsearch would flatten the arrays and give incorrect counts.

## Aggregation Structure

### Overall Architecture

The faceting system uses a **sampler aggregation** to optimize performance on large datasets (50M+ samples):

```
Top Level Query
  ↓
Sampler Aggregation (samples 100k docs per shard)
  ↓
  ├── Nested Aggregation: characteristics
  │   └── Terms Aggregation: by key
  │       └── Terms Aggregation: by value
  │
  ├── Nested Aggregation: relationships
  │   └── Terms Aggregation: by type
  │
  └── Nested Aggregation: externalReferences
      └── Terms Aggregation: by archive
```

### Characteristics Aggregation

**Location**: `AttributeFacet.java`

**Structure**: Two-level nested aggregation

```java
.nested(n -> n.path("characteristics"))           // Enter nested field
.aggregations("dynamic", a1 -> a1
    .terms(t -> t
        .field("characteristics.key.keyword")      // Level 1: Group by KEY
        .size(10)                                  // Top 10 keys
        .shardSize(200)                            // Consider top 200 per shard
    )
    .aggregations("by_value", a2 -> a2
        .terms(t2 -> t2
            .field("characteristics.value.keyword") // Level 2: Group by VALUE
            .size(10)                               // Top 10 values per key
            .shardSize(200)                         // Consider top 200 per shard
        )
    )
)
```

**Result Structure**:
```
characteristics
├── organism (key)
│   ├── Homo sapiens: 250,000 samples
│   ├── Mus musculus: 150,000 samples
│   └── ...
├── sex (key)
│   ├── male: 200,000 samples
│   ├── female: 175,000 samples
│   └── ...
└── geographic location (key)
    ├── USA: 100,000 samples
    ├── UK: 80,000 samples
    └── ...
```

**Excluded Keys**: Certain keys are excluded from faceting (see `EXCLUDED_FACETS` in `AttributeFacet.java`):
- `description`, `sample name`, `title`
- `INSDC first public`, `ENA first public`
- `collection date`, `SRA accession`
- And others that are not useful for filtering

### Relationships Aggregation

**Location**: `RelationshipFacet.java`

**Structure**: Single-level nested aggregation

```java
.nested(n -> n.path("relationships"))              // Enter nested field
.aggregations("by_type", a1 -> a1
    .terms(t -> t
        .field("relationships.type.keyword")       // Group by relationship type
        .size(10)                                   // Top 10 types
    )
)
```

**Result Structure**:
```
relationships
└── relationship type
    ├── derived from: 133,000 samples
    ├── parent of: 100,000 samples
    ├── child of: 85,000 samples
    └── ...
```

**Note**: The code also supports aggregating by `source` and `target` (see `getAggregationsWithSourceAndTarget()`), but the default only uses `type`.

### External References Aggregation

**Location**: `ExternalRefFacet.java`

**Structure**: Single-level nested aggregation

```java
.nested(n -> n.path("externalReferences"))         // Enter nested field
.aggregations("by_archive", a1 -> a1
    .terms(t -> t
        .field("externalReferences.archive.keyword")  // Group by archive type
        .size(10)                                   // Top 10 archives
        .minDocCount(1)                             // Include all archives
    )
)
```

**Result Structure**:
```
externalReferences
└── external archive
    ├── ENA: 333,000 samples
    ├── SRA: 250,000 samples
    ├── GEO: 50,000 samples
    └── ...
```

## Sampling and Extrapolation

### Why Sampling?

For a dataset with 50 million samples, running aggregations on all documents would be:
- **Slow**: Could take minutes
- **Memory-intensive**: Requires significant heap space
- **Unnecessary**: Approximate counts are sufficient for faceting

### How Sampling Works

**Location**: `SamplingFacetingStrategy.java` (lines 56-63)

```java
.sampler(s -> s.shardSize(100000))  // Sample 100k documents per shard
.aggregations(subAggregations)       // Run nested aggregations on sampled docs
```

**Process**:
1. **Sampling**: Randomly selects 100,000 documents per shard
   - With 3 shards: ~300,000 total documents sampled
   - Sampling rate: 300k / 50M = 0.6%

2. **Aggregation**: Runs nested aggregations on sampled documents only
   - Much faster than processing all 50M documents
   - Results are approximate but statistically valid

3. **Extrapolation**: Scales up the counts to estimate full dataset

### Extrapolation Factor

**Location**: `SamplingFacetingStrategy.java` (lines 144-150)

```java
if (sampledDocs > 0 && totalDocs > 0) {
    extrapolationFactor = (double) totalDocs / sampledDocs;
}
```

**Example**:
- Total documents matching query: 50,000,000
- Sampled documents: 300,000
- Extrapolation factor: 50M / 300k = **166.67**

**Application**: Each sampled count is multiplied by the factor

```java
// From AttributeFacet.java
long keyCount = Math.round(bucket.docCount() * extrapolationFactor);
long valueCount = Math.round(valueBucket.docCount() * extrapolationFactor);
```

**Example Result**:
- Sampled: "Homo sapiens" appears 1,500 times in 300k samples
- Extrapolated: 1,500 × 166.67 = **~250,000** samples in full dataset

## Complete Flow Example

### Query: "All public samples" (50M results)

```
1. Query Execution
   └── Matches 50,000,000 sample documents

2. Sampler Aggregation
   └── Randomly selects 300,000 samples (100k per shard × 3 shards)

3. Nested Aggregations (run on 300k samples)
   ├── characteristics
   │   ├── organism → {Homo sapiens: 1,500, Mus musculus: 900}
   │   └── sex → {male: 1,200, female: 1,050}
   │
   ├── relationships
   │   └── by_type → {derived from: 800, parent of: 600}
   │
   └── externalReferences
       └── by_archive → {ENA: 2,000, SRA: 1,500}

4. Extrapolation Calculation
   └── Factor: 50M / 300k = 166.67

5. Final Facets (extrapolated counts)
   ├── organism
   │   ├── Homo sapiens: 250,000 (1,500 × 166.67)
   │   └── Mus musculus: 150,000 (900 × 166.67)
   │
   ├── sex
   │   ├── male: 200,000 (1,200 × 166.67)
   │   └── female: 175,000 (1,050 × 166.67)
   │
   ├── relationship type
   │   ├── derived from: 133,000 (800 × 166.67)
   │   └── parent of: 100,000 (600 × 166.67)
   │
   └── external archive
       ├── ENA: 333,000 (2,000 × 166.67)
       └── SRA: 250,000 (1,500 × 166.67)
```

## Code Locations

### Key Files

1. **`SamplingFacetingStrategy.java`**
   - Builds the aggregation structure
   - Implements sampling and extrapolation
   - Extracts and processes facet results

2. **`AttributeFacet.java`**
   - Builds characteristics aggregation (two-level)
   - Processes characteristics facet results
   - Excludes certain keys from faceting

3. **`RelationshipFacet.java`**
   - Builds relationships aggregation
   - Processes relationship facet results

4. **`ExternalRefFacet.java`**
   - Builds external references aggregation
   - Processes external reference facet results

5. **`FacetService.java`**
   - Orchestrates facet retrieval
   - Builds Elasticsearch queries with aggregations
   - Calls strategy to retrieve facets

### Model Classes

- **`Sample.java`**: Main document model with nested fields
- **`Attribute.java`**: Characteristics key-value pairs
- **`Relationship.java`**: Relationship records
- **`ExternalReference.java`**: External reference records

## Performance Considerations

### Current Configuration

- **Sampler size**: 100,000 documents per shard
- **Total samples**: ~300,000 (with 3 shards)
- **Sampling rate**: ~0.6% for 50M dataset
- **Extrapolation factor**: ~166x

### Aggregation Parameters

- **`size`**: Top 10 values returned per aggregation
- **`shardSize`**: Top 200 candidates per shard (for characteristics)
- **Timeout**: 60 seconds (recently increased from 30s)

### Optimization Opportunities

See `docs/faceting-strategy-plan.md` for proposed improvements:
- Increase sampler size to 500k (1% sampling rate)
- Increase `shardSize` to 1000 for better accuracy
- Implement adaptive sampling based on result set size
- Add caching for common queries

## Accuracy Notes

### Sampling Accuracy

- **Common values** (> 1% prevalence): ±10-20% accuracy
- **Rare values** (< 0.1% prevalence): May be missed or inaccurate
- **Extrapolation error**: Amplified by factor of 166x

### Limitations

1. **Rare values**: Values appearing < 10 times in sample may be inaccurate
2. **Top-N limitation**: Only top 10 values per facet are returned
3. **Shard-level merging**: With `shardSize=200`, only 600 candidates considered (200 × 3 shards) before merging to top 10

### Future Improvements

- Increase sampling rate to 1-2% for better accuracy
- Increase `shardSize` to 1000 for better top-value accuracy
- Implement confidence intervals for facet counts
- Flag low-confidence values (< 10 sampled occurrences)

## Related Documentation

- **`docs/faceting-strategy-plan.md`**: Comprehensive plan for improving faceting performance and accuracy
- **Elasticsearch Nested Aggregations**: [Official Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-nested-aggregation.html)
- **Elasticsearch Sampler Aggregation**: [Official Documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-sampler-aggregation.html)

---

**Last Updated**: 2025-01-23  
**Author**: Documentation generated from codebase analysis
