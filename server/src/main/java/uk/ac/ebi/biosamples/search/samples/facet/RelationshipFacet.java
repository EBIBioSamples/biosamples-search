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
        .aggregations(RelationshipFacetType.TYPE.type, a1 -> a1
            .terms(t -> t
                .field("relationships.type.keyword")
                .size(10)
            )
        )
    );
  }

  public static Aggregation getAggregationsWithSourceAndTarget() {
    return Aggregation.of(a -> a
        .nested(n -> n.path("relationships"))
        .aggregations(RelationshipFacetType.TYPE.type, a1 -> a1
            .terms(t -> t
                .field("relationships.type.keyword")
                .size(10)
            )
        )
        .aggregations(RelationshipFacetType.SOURCE.type, a1 -> a1
            .terms(t -> t
                .field("relationships.source.keyword")
                .size(10)
            )
        )
        .aggregations(RelationshipFacetType.TARGET.type, a1 -> a1
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
    long keyCount = Math.round(nestedAggResult.docCount() * extrapolationFactor);
    populateRelationshipFacets(RelationshipFacetType.TYPE, nestedAggResult, facets, keyCount, extrapolationFactor);
    populateRelationshipFacets(RelationshipFacetType.SOURCE, nestedAggResult, facets, keyCount, extrapolationFactor);
    populateRelationshipFacets(RelationshipFacetType.TARGET, nestedAggResult, facets, keyCount, extrapolationFactor);

    return facets;
  }

  private static void populateRelationshipFacets(RelationshipFacetType facetType, NestedAggregate nestedAggResult,
                                                 List<Facet> facets, long keyCount, double extrapolationFactor) {
    Map<String, Long> facetBuckets;
    List<StringTermsBucket> stBuckets;
    if (nestedAggResult.aggregations().get(facetType.type) != null) {
      facetBuckets = new HashMap<>();
      facets.add(new Facet("rel", facetType.field, keyCount, facetBuckets));
      stBuckets = nestedAggResult.aggregations().get(facetType.type).sterms().buckets().array();
      for (StringTermsBucket bucket : stBuckets) {
        long valueCount = Math.round(bucket.docCount() * extrapolationFactor);
        String keyKey = bucket.key().stringValue();
        facetBuckets.put(keyKey, valueCount);
      }
    }
  }

  enum RelationshipFacetType {
    TYPE("by_type", "relationship type"),
    SOURCE("by_source", "relationship source"),
    TARGET("by_target", "relationship target");

    final String type;
    final String field;

    RelationshipFacetType(String type, String field) {
      this.type = type;
      this.field = field;
    }
  }
}
