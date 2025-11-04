package uk.ac.ebi.biosamples.search.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.biosamples.search.es.QueryHelper;
import uk.ac.ebi.biosamples.search.samples.Sample;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FacetService {
  private final ElasticsearchOperations elasticsearchOperations;
  private final FacetingStrategy facetingStrategy;

  public FacetService(ElasticsearchOperations elasticsearchOperations,
                      @Qualifier("samplingFacetingStrategy") FacetingStrategy facetingStrategy) {
    this.elasticsearchOperations = elasticsearchOperations;
    this.facetingStrategy = facetingStrategy;
  }

  public List<Facet> getFacets(SearchQuery searchQuery) {
    Query esSearchQuery = QueryHelper.getSearchQuery(searchQuery);
    Map<String, Aggregation> aggregations = getAggregations(searchQuery);
    NativeQuery query = getEsNativeQuery(esSearchQuery, aggregations);
    return retrieveFacets(query);
  }

  private Map<String, Aggregation> getAggregations(SearchQuery searchQuery) {
    if (!CollectionUtils.isEmpty(searchQuery.getFacets())) {
      Map<String, Aggregation> aggregationMap = new HashMap<>();
      aggregationMap.put("characteristics", AttributeFacet.getAggregations(searchQuery.getFacets()));
      return aggregationMap;
    }
    return facetingStrategy.getDefaultAggregations();
  }

  private NativeQuery getEsNativeQuery(Query searchQuery, Map<String, Aggregation> aggregations) {
    NativeQueryBuilder builder = NativeQuery.builder()
        .withQuery(searchQuery)
        .withMaxResults(0)
        .withTimeout(Duration.ofSeconds(30));

    if (!aggregations.isEmpty()) {
      aggregations.forEach(builder::withAggregation);
    }

    return builder.build();
  }

  public List<Facet> retrieveFacets(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    return facetingStrategy.retrieveFacets(hits);
  }
}
