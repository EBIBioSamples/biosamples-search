package uk.ac.ebi.biosamples.search.samples.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalRefFacet {

  public static Aggregation getAggregations() {
    return Aggregation.of(a -> a
        .nested(n -> n.path("externalReferences"))
        .aggregations("by_archive", a1 -> a1
            .terms(t -> t
                .field("externalReferences.archive.keyword")
                .size(10)
                .minDocCount(1)
            )
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(Aggregate aggregate, double extrapolationFactor) {
    NestedAggregate nestedAggResult = aggregate.nested();
    return populateFacets(nestedAggResult, extrapolationFactor);
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    NestedAggregate nestedAggResult = aggregation.aggregation().getAggregate().nested();
    return populateFacets(nestedAggResult, 1.0);
  }

  private static List<Facet> populateFacets(NestedAggregate nestedAggResult, double extrapolationFactor) {
    List<Facet> facets = new ArrayList<>();
    Map<String, Long> facetBuckets = new HashMap<>();
    long keyCount = Math.round(nestedAggResult.docCount() * extrapolationFactor);
    facets.add(new Facet("extd", "external ref", keyCount, facetBuckets));
    List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_archive").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long valueCount = Math.round(bucket.docCount() * extrapolationFactor);
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, valueCount);
    }

    return facets;
  }
}
