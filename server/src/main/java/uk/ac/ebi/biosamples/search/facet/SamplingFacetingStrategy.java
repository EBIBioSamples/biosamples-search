package uk.ac.ebi.biosamples.search.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.samples.Sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("samplingFacetingStrategy")
public class SamplingFacetingStrategy implements FacetingStrategy {

  @Override
  public Map<String, Aggregation> getDefaultAggregations() {
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

  @Override
  public List<Facet> retrieveFacets(SearchHits<Sample> hits) {
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
}
