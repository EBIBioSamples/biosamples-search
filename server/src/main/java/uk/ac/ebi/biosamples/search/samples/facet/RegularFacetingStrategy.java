package uk.ac.ebi.biosamples.search.samples.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.samples.Sample;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("regularFacetingStrategy")
public class RegularFacetingStrategy implements FacetingStrategy {

  public Map<String, Aggregation> getDefaultAggregations() {
    Map<String, Aggregation> aggregationMap = new HashMap<>();
    aggregationMap.put("characteristics", AttributeFacet.getAggregations());
    aggregationMap.put("relationships", RelationshipFacet.getAggregations());
    aggregationMap.put("update", DateRangeFacet.getAggregations());
    aggregationMap.put("externalReferences", ExternalRefFacet.getAggregations());
    return aggregationMap;
  }

  public List<Facet> retrieveFacets(SearchHits<Sample> hits) {
    List<Facet> facets = new ArrayList<>();

    ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();
    if (aggregations == null) {
      return facets;
    }

    Map<String, ElasticsearchAggregation> aggMap = aggregations.aggregationsAsMap();
    if (aggMap.containsKey("characteristics")) {
      facets.addAll(AttributeFacet.populateFacetFromAggregationResults(aggMap.get("characteristics")));
    }
    if (aggMap.containsKey("relationships")) {
      facets.addAll(RelationshipFacet.populateFacetFromAggregationResults(aggMap.get("relationships")));
    }
    if (aggMap.containsKey("update")) {
      facets.addAll(DateRangeFacet.populateFacetFromAggregationResults(aggMap.get("update")));
    }
    if (aggMap.containsKey("externalReferences")) {
      facets.addAll(ExternalRefFacet.populateFacetFromAggregationResults(aggMap.get("externalReferences")));
    }

    return facets;
  }
}
