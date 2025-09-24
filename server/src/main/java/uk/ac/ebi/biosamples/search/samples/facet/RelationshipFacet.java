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

public class RelationshipFacet {

  public static Aggregation getAggregations() {
    return Aggregation.of(a -> a
        .nested(n -> n.path("relationships"))
        .aggregations("by_type", a1 -> a1
            .terms(t -> t
                .field("relationships.type.keyword")
                .size(10)
            )
        )
        .aggregations("by_source", a1 -> a1
            .terms(t -> t
                .field("relationships.source.keyword")
                .size(10)
            )
        )
        .aggregations("by_target", a1 -> a1
            .terms(t -> t
                .field("relationships.target.keyword")
                .size(10)
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
    facets.add(new Facet("rel", "relationship type", keyCount, facetBuckets));
    List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_type").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long valueCount = Math.round(bucket.docCount() * extrapolationFactor);
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, valueCount);
    }

    facetBuckets = new HashMap<>();
    facets.add(new Facet("rel", "relationship source", nestedAggResult.docCount(), facetBuckets));
    stBuckets = nestedAggResult.aggregations().get("by_source").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long valueCount = Math.round(bucket.docCount() * extrapolationFactor);
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, valueCount);
    }

    facetBuckets = new HashMap<>();
    facets.add(new Facet("rel", "relationship target", nestedAggResult.docCount(), facetBuckets));
    stBuckets = nestedAggResult.aggregations().get("by_target").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long valueCount = Math.round(bucket.docCount() * extrapolationFactor);
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, valueCount);
    }

    return facets;
  }
}
