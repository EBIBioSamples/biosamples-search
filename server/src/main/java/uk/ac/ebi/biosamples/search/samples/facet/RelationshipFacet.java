package uk.ac.ebi.biosamples.search.samples.facet;

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
            .terms(t -> t.field("relationships.type.keyword"))
        )
        .aggregations("by_source", a1 -> a1
            .terms(t -> t.field("relationships.source.keyword"))
        )
        .aggregations("by_target", a1 -> a1
            .terms(t -> t.field("relationships.target.keyword"))
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    List<Facet> facets = new ArrayList<>();
    NestedAggregate nestedAggResult = aggregation.aggregation().getAggregate().nested();

    Map<String, Long> facetBuckets = new HashMap<>();
    facets.add(new Facet("rel", "relationship type", nestedAggResult.docCount(), facetBuckets));
    List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_type").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, keyCount);
    }

    facetBuckets = new HashMap<>();
    facets.add(new Facet("rel", "relationship source", nestedAggResult.docCount(), facetBuckets));
    stBuckets = nestedAggResult.aggregations().get("by_source").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, keyCount);
    }

    facetBuckets = new HashMap<>();
    facets.add(new Facet("rel", "relationship target", nestedAggResult.docCount(), facetBuckets));
    stBuckets = nestedAggResult.aggregations().get("by_target").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, keyCount);
    }

    return facets;
  }
}
