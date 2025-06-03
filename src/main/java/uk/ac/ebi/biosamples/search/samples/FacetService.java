package uk.ac.ebi.biosamples.search.samples;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.es.QueryHelper;
import uk.ac.ebi.biosamples.search.samples.facet.AttributeFacet;
import uk.ac.ebi.biosamples.search.samples.facet.DateRangeFacet;
import uk.ac.ebi.biosamples.search.samples.facet.Facet;
import uk.ac.ebi.biosamples.search.samples.facet.RelationshipFacet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacetService {
  private final ElasticsearchOperations elasticsearchOperations;

  public List<Facet> getFacets(SearchQuery searchQuery) {
    Query esSearchQuery = QueryHelper.getSearchQuery(searchQuery);
    Map<String, Aggregation> aggregations = getAggregations(searchQuery);
    NativeQuery query = getEsNativeQuery(esSearchQuery, aggregations);
    return retrieveFacets(query);
  }

  private Map<String, Aggregation> getAggregations(SearchQuery searchQuery) {
    Map<String, Aggregation> aggregationMap = new HashMap<>();
    aggregationMap.put("characteristics", AttributeFacet.getAggregations());
    aggregationMap.put("relationships", RelationshipFacet.getAggregations());
    aggregationMap.put("update", DateRangeFacet.getAggregations());
    return aggregationMap;
  }

  private NativeQuery getEsNativeQuery(Query searchQuery, Map<String, Aggregation> aggregations) {
    NativeQueryBuilder builder = NativeQuery.builder()
        .withQuery(searchQuery)
        .withMaxResults(0);

    if (!aggregations.isEmpty()) {
      aggregations.forEach(builder::withAggregation);
    }

    return builder.build();
  }

  private List<Facet> retrieveFacets(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getAggregations().toString());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    List<Facet> facets = new ArrayList<>();

    ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();
    if (aggregations == null) {
      return facets;
    }

    Map<String, ElasticsearchAggregation> aggMap = aggregations.aggregationsAsMap();
    facets.addAll(AttributeFacet.populateFacetFromAggregationResults(aggMap.get("characteristics")));
    facets.addAll(RelationshipFacet.populateFacetFromAggregationResults(aggMap.get("relationships")));
    facets.addAll(DateRangeFacet.populateFacetFromAggregationResults(aggMap.get("update")));

    return facets;
  }

}
