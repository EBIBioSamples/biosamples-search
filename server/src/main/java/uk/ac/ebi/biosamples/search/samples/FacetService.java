package uk.ac.ebi.biosamples.search.samples;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
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
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.biosamples.search.es.QueryHelper;
import uk.ac.ebi.biosamples.search.samples.facet.*;

import java.time.Duration;
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
    if (!CollectionUtils.isEmpty(searchQuery.getFacets())) {
      Map<String, Aggregation> aggregationMap = new HashMap<>();
      aggregationMap.put("characteristics", AttributeFacet.getAggregations(searchQuery.getFacets()));
      return aggregationMap;
    }
    return getDefaultAggregations();
  }

  private Map<String, Aggregation> getDefaultAggregations() {
    Aggregation totalCountAgg = Aggregation.of(a -> a
        .valueCount(v -> v.field("accession.keyword"))
    );

    Map<String, Aggregation> subAggregations = new HashMap<>();
    subAggregations.put("total_sampled", totalCountAgg);
    subAggregations.put("characteristics", AttributeFacet.getAggregations());
    subAggregations.put("relationships", RelationshipFacet.getAggregations());
    subAggregations.put("externalReferences", ExternalRefFacet.getAggregations());

    Aggregation samplerAgg = new Aggregation.Builder()
        .sampler(s -> s.shardSize(100000))
        .aggregations(subAggregations)
        .build();

    Map<String, Aggregation> aggregationMap = new HashMap<>();
    aggregationMap.put("sampled_facets", samplerAgg);
    aggregationMap.put("update", DateRangeFacet.getAggregations());
    aggregationMap.put("total_docs", totalCountAgg);

    return aggregationMap;
  }

  private Map<String, Aggregation> getDefaultAggregations1() {
    Map<String, Aggregation> aggregationMap = new HashMap<>();
    aggregationMap.put("characteristics", AttributeFacet.getAggregations());
    aggregationMap.put("relationships", RelationshipFacet.getAggregations());
    aggregationMap.put("update", DateRangeFacet.getAggregations());
    aggregationMap.put("externalReferences", ExternalRefFacet.getAggregations());
    return aggregationMap;
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

  private List<Facet> retrieveFacets(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    List<Facet> facets = new ArrayList<>();

    ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();
    if (aggregations == null) {
      return facets;
    }

    Map<String, ElasticsearchAggregation> aggMap = aggregations.aggregationsAsMap();

    long totalDocs = 0;
    long sampledDocs = 0;
    double extrapolationFactor = 1.0;

    if (aggMap.containsKey("total_docs")) {
      totalDocs = (long) aggMap.get("total_docs").aggregation().getAggregate().valueCount().value();
    }


    if (aggMap.containsKey("sampled_facets")) {
      ElasticsearchAggregation sampledAggContainer = aggMap.get("sampled_facets");
      if (sampledAggContainer != null && sampledAggContainer.aggregation().getAggregate().isSampler()) {
        Map<String, Aggregate> subAggs = sampledAggContainer.aggregation().getAggregate().sampler().aggregations();


        if (subAggs.containsKey("total_sampled")) {
          sampledDocs = (long) subAggs.get("total_sampled").valueCount().value();
        }

        if (sampledDocs > 0 && totalDocs > 0) {
          extrapolationFactor = (double) totalDocs / sampledDocs;
        }

        if (subAggs.containsKey("characteristics")) {
          facets.addAll(AttributeFacet.populateFacetFromAggregationResults(subAggs.get("characteristics"), extrapolationFactor));
        }
        if (subAggs.containsKey("relationships")) {
          facets.addAll(RelationshipFacet.populateFacetFromAggregationResults(subAggs.get("relationships"), extrapolationFactor));
        }
        if (subAggs.containsKey("externalReferences")) {
          facets.addAll(ExternalRefFacet.populateFacetFromAggregationResults(subAggs.get("externalReferences"), extrapolationFactor));
        }
      }
    }

    if (aggMap.containsKey("update")) {
      facets.addAll(DateRangeFacet.populateFacetFromAggregationResults(aggMap.get("update")));
    }

    return facets;
  }

  private List<Facet> retrieveFacets1(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
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
