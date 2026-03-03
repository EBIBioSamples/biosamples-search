# Faceting Strategy Plan for 50M+ Samples
## 3-Node, 3-Shard Elasticsearch Cluster

### Executive Summary
This document outlines a comprehensive faceting strategy optimized for a 50 million sample dataset on a 3-node, 3-shard Elasticsearch cluster, with scalability considerations for future growth.

---

## 1. Current State Analysis

### 1.1 Current Configuration
- **Cluster**: 3 nodes, 3 shards (primary shards only)
- **Data Distribution**: ~16.7M documents per shard
- **Current Strategy**: `SamplingFacetingStrategy`
- **Sampler Size**: 100,000 documents per shard (300k total)
- **Sampling Rate**: 0.6% (300k / 50M)
- **Extrapolation Factor**: ~166x (50M / 300k)
- **Terms Aggregation**:
  - `size`: 10 (top 10 values returned)
  - `shardSize`: 200 (top 200 per shard before merging)

### 1.2 Current Facet Types
1. **Characteristics** (nested field)
   - Dynamic attributes (key-value pairs)
   - Excludes: description, sample name, title, etc.
   - Includes: organism, sex, geographic location, etc.
2. **Relationships** (nested field)
   - Relationship types, sources, targets
3. **External References** (nested field)
   - Archive types (ENA, SRA, etc.)
4. **Date Ranges**
   - Update dates (yearly histogram)
   - Runs on ALL documents (not sampled)

### 1.3 Identified Issues

#### Accuracy Problems
- **Low Sampling Rate (0.6%)**: 
  - Rare values (< 0.6% prevalence) may be completely missed
  - Extrapolation factor of 166x amplifies small counting errors
  - Example: If a value appears 1 time in 300k samples, it extrapolates to 166, but could be 0 or 500 in reality

#### Performance Problems
- **Small `shardSize` (200)**: 
  - May miss important terms that rank differently across shards
  - With 3 shards, only 600 candidate terms considered before merging to top 10
  - High-cardinality fields (e.g., organism with thousands of species) may show incorrect top values

#### Scalability Problems
- **Fixed Sampler Size**: 
  - Doesn't adapt to query result set size
  - For filtered queries returning 1M docs, 300k sample is good (30%)
  - For filtered queries returning 50M docs, 300k sample is poor (0.6%)
  - For filtered queries returning 100k docs, 300k sample is wasteful

#### Missing Features
- **No Caching**: Every facet request hits Elasticsearch
- **No Pagination**: Can only see top 10 values per facet
- **No Adaptive Strategy**: Same approach for all query sizes
- **No Timeout Handling**: 30s timeout may not be enough for complex queries

---

## 2. Proposed Multi-Tier Strategy

### 2.1 Strategy Selection Logic

The system should automatically choose the best faceting strategy based on the query result set size:

```
IF resultSetSize < 100,000:
    → Use RegularFacetingStrategy (no sampling, full accuracy)
    
ELSE IF resultSetSize < 1,000,000:
    → Use AdaptiveSamplingStrategy (10-20% sampling rate)
    
ELSE IF resultSetSize < 10,000,000:
    → Use OptimizedSamplingStrategy (1-5% sampling rate)
    
ELSE (resultSetSize >= 10,000,000):
    → Use HighVolumeSamplingStrategy (0.5-1% sampling rate)
```

**Rationale**: 
- Small result sets (< 100k) can be fully aggregated quickly
- Medium result sets (100k-1M) benefit from moderate sampling
- Large result sets (1M-10M) need aggressive sampling but maintain accuracy
- Very large result sets (10M+) prioritize speed over perfect accuracy

### 2.2 Optimized Sampling Strategy (Primary Focus)

For the 50M dataset, this will be the default strategy for unfiltered queries.

#### 2.2.1 Improved Sampler Configuration

**Current**: 100k per shard = 300k total
**Proposed**: Adaptive based on result set size

```
samplerSize = min(
    max(resultSetSize * 0.01, 500000),  // At least 1% of result set, minimum 500k
    2000000                              // Maximum 2M samples (memory limit)
)
perShardSize = samplerSize / numShards
```

**Example for 50M docs**:
- `samplerSize = min(50M * 0.01, 2M) = 500k`
- `perShardSize = 500k / 3 = ~167k per shard`
- **Sampling Rate**: 1% (vs current 0.6%)
- **Extrapolation Factor**: 100x (vs current 166x)

**Benefits**:
- Better accuracy (1% vs 0.6%)
- Lower extrapolation error (100x vs 166x)
- Still fast (500k samples process quickly)

#### 2.2.2 Enhanced Terms Aggregation

**Current**: `size=10, shardSize=200`
**Proposed**: `size=20, shardSize=1000`

**Rationale**:
- **`size=20`**: Return top 20 values instead of 10 (better UX, minimal performance impact)
- **`shardSize=1000`**: 
  - With 3 shards, consider 3000 candidate terms before merging
  - Better chance of finding correct top values across shards
  - Still manageable memory footprint

**Memory Impact**:
- Per shard: 1000 terms × ~50 bytes = ~50KB per aggregation
- With 3 nested aggregations (characteristics, relationships, externalRefs) = ~150KB per shard
- Total: ~450KB across cluster (negligible)

#### 2.2.3 Smart Field Selection

**Problem**: Some characteristics fields have very high cardinality (thousands of unique values)
**Solution**: Use different strategies for different field types

**High-Cardinality Fields** (e.g., organism, geographic location):
- Use `shardSize=2000` (more candidates)
- Use `minDocCount=2` (filter out singletons in sampled set)
- Apply extrapolation with confidence intervals

**Low-Cardinality Fields** (e.g., sex, ENA-CHECKLIST):
- Use `shardSize=500` (fewer candidates needed)
- Use `minDocCount=1`
- Standard extrapolation

#### 2.2.4 Composite Aggregation for Pagination

**Current Limitation**: Can only see top 10-20 values
**Solution**: Use Elasticsearch `composite` aggregation for paginated facet browsing

**Implementation**:
- For characteristics: Composite aggregation on `[key, value]`
- Supports `after_key` parameter for pagination
- Client can request "next page" of facet values
- Useful for browsing all organisms, all geographic locations, etc.

**Trade-off**: 
- Slightly slower than terms aggregation
- But enables "show all" functionality for power users

### 2.3 Caching Strategy

#### 2.3.1 Query Result Caching

**Cache Key**: Hash of query + filter combination
**Cache TTL**: 
- Unfiltered queries: 5 minutes (data changes frequently)
- Filtered queries: 15 minutes (more stable)
- Highly filtered queries (< 1M results): 30 minutes

**Cache Storage**: 
- In-memory cache (Caffeine or similar)
- Size limit: 100MB (approximately 100-200 cached facet results)
- Eviction: LRU (Least Recently Used)

**Cache Invalidation**:
- On new sample indexing (invalidate all)
- On sample update (invalidate matching filters)
- Manual invalidation endpoint for admin

#### 2.3.2 Popular Facet Caching

**Problem**: Common queries (e.g., "all public samples") are requested frequently
**Solution**: Pre-compute and cache popular facet combinations

**Popular Queries to Cache**:
1. Unfiltered public samples
2. Public samples by domain
3. Recent samples (last 30 days)
4. Samples by top 10 organisms

**Update Frequency**: Every 10 minutes (background job)

### 2.4 Performance Optimizations

#### 2.4.1 Global Ordinals

**Problem**: Nested field aggregations are slower than regular field aggregations
**Solution**: Enable `eager_global_ordinals` for frequently aggregated fields

**Fields to Optimize**:
- `characteristics.key.keyword`
- `characteristics.value.keyword`
- `relationships.type.keyword`

**Trade-off**: 
- Slightly slower indexing (ordinals built on refresh)
- Much faster aggregation queries
- Worth it for read-heavy workloads

#### 2.4.2 Doc Values Optimization

**Ensure**: All `.keyword` fields use `doc_values: true` (default, but verify)
**Benefit**: Faster aggregations, lower memory usage

#### 2.4.3 Request Timeout Strategy

**Current**: Fixed 30 seconds
**Proposed**: Adaptive timeout

```
IF resultSetSize < 1M:
    timeout = 10 seconds
ELSE IF resultSetSize < 10M:
    timeout = 30 seconds
ELSE:
    timeout = 60 seconds
```

**Fallback**: If timeout occurs, return partial results with a flag indicating incompleteness

### 2.5 Accuracy Improvements

#### 2.5.1 Confidence Intervals

**Problem**: Extrapolated counts are estimates, but we don't know how accurate
**Solution**: Calculate confidence intervals based on sampling statistics

**Formula** (simplified):
```
estimatedCount = sampledCount * extrapolationFactor
marginOfError = 1.96 * sqrt(sampledCount) * extrapolationFactor  // 95% confidence
confidenceInterval = [estimatedCount - marginOfError, estimatedCount + marginOfError]
```

**Display**: Show counts as "~1,000,000 (±50,000)" in UI

#### 2.5.2 Rare Value Detection

**Problem**: Values appearing < 10 times in sample may be inaccurate
**Solution**: Flag low-confidence values

**Logic**:
```
IF sampledCount < 10:
    confidence = "low"
    display = "~" + estimatedCount + " (approximate)"
ELSE IF sampledCount < 100:
    confidence = "medium"
    display = estimatedCount
ELSE:
    confidence = "high"
    display = estimatedCount
```

#### 2.5.3 Stratified Sampling (Future Enhancement)

**Advanced**: Instead of random sampling, use stratified sampling
- Sample proportionally from each shard
- Weight by document distribution
- More accurate for skewed data

**Complexity**: High - requires custom aggregation logic
**Benefit**: Better accuracy for rare values

---

## 3. Implementation Phases

### Phase 1: Quick Wins (Week 1)
**Goal**: Improve current strategy with minimal code changes

1. **Increase Sampler Size**
   - Change from 100k to 167k per shard (500k total)
   - Improves accuracy from 0.6% to 1% sampling rate

2. **Increase Terms Aggregation Parameters**
   - Change `size` from 10 to 20
   - Change `shardSize` from 200 to 1000
   - Better top-value accuracy

3. **Add Adaptive Timeout**
   - Implement timeout logic based on result set size
   - Prevents premature timeouts

**Expected Impact**:
- Accuracy: +40% (1% vs 0.6% sampling)
- Performance: Similar (slightly slower, but acceptable)
- UX: Better (20 values vs 10)

### Phase 2: Adaptive Strategy (Week 2-3)
**Goal**: Implement strategy selection based on result set size

1. **Result Set Size Estimation**
   - Add `value_count` aggregation before main query
   - Or use `track_total_hits: true` in search
   - Cache result set size for strategy selection

2. **Strategy Selection Logic**
   - Implement `if/else` logic in `FacetService`
   - Route to appropriate strategy based on size

3. **RegularFacetingStrategy for Small Queries**
   - Use existing `RegularFacetingStrategy` for < 100k results
   - No sampling = perfect accuracy

**Expected Impact**:
- Small queries: Perfect accuracy, faster (no sampling overhead)
- Large queries: Better accuracy (adaptive sampling)

### Phase 3: Caching (Week 4)
**Goal**: Reduce Elasticsearch load for common queries

1. **Query Result Cache**
   - Implement Caffeine cache
   - Cache key: query + filter hash
   - Cache value: facet results

2. **Cache Invalidation**
   - On indexing events
   - Manual invalidation endpoint

3. **Monitoring**
   - Cache hit rate metrics
   - Cache size metrics

**Expected Impact**:
- Common queries: 10-100x faster (cache hit)
- Elasticsearch load: -30% to -50% reduction

### Phase 4: Advanced Features (Week 5-6)
**Goal**: Add pagination and accuracy improvements

1. **Composite Aggregation**
   - Implement for characteristics pagination
   - Add `after_key` parameter support

2. **Confidence Intervals**
   - Calculate and return with facet results
   - Display in UI (optional)

3. **Global Ordinals**
   - Enable for key fields
   - Monitor indexing performance impact

**Expected Impact**:
- UX: Pagination enables "show all" functionality
- Accuracy: Users understand estimate quality

---

## 4. Performance Targets

### 4.1 Response Time Targets

| Query Type | Current | Target | Strategy |
|------------|---------|--------|----------|
| Unfiltered (50M) | 15-30s | 10-20s | Optimized sampling |
| Filtered (1-10M) | 10-20s | 5-10s | Adaptive sampling |
| Filtered (< 1M) | 5-10s | 1-3s | Regular (no sampling) |
| Cached | N/A | < 100ms | Cache hit |

### 4.2 Accuracy Targets

| Metric | Current | Target |
|--------|---------|--------|
| Sampling Rate (50M) | 0.6% | 1-2% |
| Extrapolation Error | ±20% | ±10% |
| Rare Value Detection | Poor | Good (flag < 10 count) |

### 4.3 Scalability Targets

- **Current**: 50M samples, 3 shards
- **Target**: Support 100M+ samples with same performance
- **Approach**: 
  - Increase shards to 5-7 (when scaling)
  - Maintain 1-2% sampling rate
  - Use caching to offset increased load

---

## 5. Monitoring & Metrics

### 5.1 Key Metrics to Track

1. **Performance Metrics**
   - Facet query response time (p50, p95, p99)
   - Timeout rate
   - Cache hit rate
   - Elasticsearch query time

2. **Accuracy Metrics**
   - Sampling rate per query
   - Extrapolation factor
   - Rare value detection rate

3. **Resource Metrics**
   - Memory usage (cache size)
   - Elasticsearch CPU usage
   - Network I/O

### 5.2 Alerts

- **Response Time**: Alert if p95 > 30s
- **Timeout Rate**: Alert if > 5% of queries timeout
- **Cache Hit Rate**: Alert if < 20% (indicates cache too small or TTL too short)
- **Elasticsearch Errors**: Alert on any aggregation errors

---

## 6. Scaling Considerations

### 6.1 Horizontal Scaling (More Nodes)

**Current**: 3 nodes, 3 shards
**Future**: 5-7 nodes, 5-7 shards

**Impact on Strategy**:
- More shards = more parallel processing
- Can maintain same per-shard sampler size
- Total samples increase proportionally
- Example: 7 shards × 167k = 1.17M samples (still 1-2% of 50M)

**Action**: Strategy automatically adapts (sampler size per shard remains constant)

### 6.2 Vertical Scaling (More Memory/CPU)

**Benefit**: Can increase sampler size without performance degradation
**Action**: Increase `maxSamplerSize` from 2M to 5M

### 6.3 Data Growth (100M+ Samples)

**Challenge**: Maintaining performance as data grows
**Solutions**:
1. **Increase Shards**: 3 → 5 → 7 shards
2. **Maintain Sampling Rate**: Keep 1-2% sampling rate
3. **Aggressive Caching**: Cache more query combinations
4. **Consider Separate Facet Index**: Pre-computed facets (advanced)

---

## 7. Risk Assessment

### 7.1 High Risk

**Risk**: Increasing sampler size causes timeouts
**Mitigation**: 
- Implement adaptive timeout
- Monitor timeout rates
- Fallback to smaller sampler if timeout occurs

### 7.2 Medium Risk

**Risk**: Cache memory usage grows too large
**Mitigation**:
- Set strict size limits (100MB)
- Use LRU eviction
- Monitor memory usage

### 7.3 Low Risk

**Risk**: Accuracy improvements not noticeable to users
**Mitigation**:
- A/B test old vs new strategy
- Collect user feedback
- Monitor accuracy metrics

---

## 8. Success Criteria

### 8.1 Performance
- ✅ 90% of facet queries complete in < 20s (currently 15-30s)
- ✅ Cache hit rate > 30%
- ✅ Timeout rate < 1%

### 8.2 Accuracy
- ✅ Sampling rate ≥ 1% for 50M+ result sets
- ✅ Extrapolation error < ±10% for common values
- ✅ Rare values (< 0.1% prevalence) detected and flagged

### 8.3 Scalability
- ✅ Strategy works for 100M+ samples with same performance
- ✅ Easy to add more shards without code changes

---

## 9. Alternative Approaches (Not Recommended)

### 9.1 Pre-computed Facets
**Approach**: Maintain a separate Elasticsearch index with pre-computed facets
**Pros**: Very fast queries
**Cons**: 
- Complex to maintain
- High storage cost
- Difficult to keep in sync
- Doesn't work with dynamic filters

**Verdict**: Not recommended unless caching proves insufficient

### 9.2 Materialized Views
**Approach**: Use Elasticsearch transform API to create materialized facet views
**Pros**: Automatic updates, fast queries
**Cons**: 
- Requires Elasticsearch Platinum license
- Still need to handle dynamic filters
- Complex setup

**Verdict**: Consider if Elasticsearch license allows and caching insufficient

### 9.3 Separate Facet Service
**Approach**: Dedicated microservice for faceting with specialized optimizations
**Pros**: Can optimize independently
**Cons**: 
- Increased complexity
- Network overhead
- More services to maintain

**Verdict**: Not needed for current scale

---

## 10. Next Steps

1. **Review this plan** with team
2. **Prioritize phases** based on business needs
3. **Set up monitoring** before making changes
4. **Implement Phase 1** (quick wins)
5. **Measure impact** and iterate
6. **Proceed to Phase 2** if Phase 1 successful

---

## Appendix A: Elasticsearch Configuration Recommendations

### A.1 Index Settings

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "refresh_interval": "30s",  // Reduce refresh frequency for better indexing performance
    "index": {
      "max_result_window": 50000  // Allow deeper pagination if needed
    }
  }
}
```

### A.2 Field Mappings (Key Fields)

```json
{
  "characteristics.key.keyword": {
    "type": "keyword",
    "eager_global_ordinals": true  // Enable for faster aggregations
  },
  "characteristics.value.keyword": {
    "type": "keyword",
    "eager_global_ordinals": true
  }
}
```

### A.3 Cluster Settings

- **Heap Size**: At least 50% of available RAM (but not more than 32GB)
- **Field Data Cache**: Monitor usage, increase if needed
- **Query Cache**: Enable (default)

---

## Appendix B: Example Query Performance

### B.1 Current Performance (Estimated)

**Query**: Unfiltered public samples (50M results)
- **Sampler**: 300k samples (0.6%)
- **Time**: 15-30 seconds
- **Accuracy**: ±20% for common values

### B.2 Phase 1 Performance (Estimated)

**Query**: Unfiltered public samples (50M results)
- **Sampler**: 500k samples (1%)
- **Time**: 12-25 seconds
- **Accuracy**: ±15% for common values

### B.3 Phase 2 Performance (Estimated)

**Query**: Filtered by organism (5M results)
- **Strategy**: Adaptive (500k samples, 10% sampling)
- **Time**: 8-15 seconds
- **Accuracy**: ±8% for common values

**Query**: Filtered by domain (500k results)
- **Strategy**: Regular (no sampling)
- **Time**: 2-5 seconds
- **Accuracy**: Perfect (100%)

---

**Document Version**: 1.0  
**Last Updated**: 2025-12-22  
**Author**: AI Assistant  
**Status**: Draft - Pending Review

