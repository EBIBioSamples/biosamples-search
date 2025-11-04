package uk.ac.ebi.biosamples.search.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeFacet {
  private static final List<String> EXCLUDED_FACETS = List.of(
      "description", "sample name", "title", "sample comment", "INSDC first public", "ENA first public",
      "INSDC secondary accession", "INSDC last update", "ENA last update", "collection date", "SRA accession",
      "External Id", "Submitter Id", "INSDC center alias", "project name", "geographic location (longitude)",
      "geographic location (latitude)",
      "INSDC status");
  private static final List<String> STATIC_FACETS = List.of(
      "organism", "sex", "INSDC center name", "ENA-CHECKLIST", "geographic location (country and/or sea)",
      "NCBI submission model");

  public static Aggregation getAggregations(List<String> facets) {
    return Aggregation.of(a -> a
        .nested(n -> n.path("characteristics"))
        .aggregations("dynamic", a1 -> a1
            .terms(t -> t
                .field("characteristics.key.keyword")
                .include(e -> e.terms(facets))
                .size(10)
            )
            .aggregations("by_value", a2 -> a2
                .terms(t2 -> t2
                    .field("characteristics.value.keyword")
                    .size(10)
                )
            )
        ));
  }

  public static Aggregation getAggregations() {
    return Aggregation.of(a -> a
        .nested(n -> n.path("characteristics"))
        .aggregations("dynamic", a1 -> a1
            .terms(t -> t
                .field("characteristics.key.keyword")
                .exclude(e -> e.terms(EXCLUDED_FACETS))
                .size(10)
                .shardSize(200)
            )
            .aggregations("by_value", a2 -> a2
                .terms(t2 -> t2
                    .field("characteristics.value.keyword")
                    .size(10)
                    .shardSize(200)
                )
            )
        )
//        .aggregations("static", a1 -> a1
//            .terms(t -> t
//                .field("characteristics.key.keyword")
//                .include(i -> i.terms(STATIC_FACETS))
//                .size(10)
//                .shardSize(200)
//            )
//            .aggregations("by_value", a2 -> a2
//                .terms(t2 -> t2
//                    .field("characteristics.value.keyword")
//                    .size(10)
//                    .shardSize(200)
//                )
//            )
//        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    NestedAggregate nestedAggResult = aggregation.aggregation().getAggregate().nested();
    return populateFacets(nestedAggResult, 1.0);
  }

  public static List<Facet> populateFacetFromAggregationResults(Aggregate aggregate, double extrapolationFactor) {
    NestedAggregate nestedAggResult = aggregate.nested();
    return populateFacets(nestedAggResult, extrapolationFactor);
  }

  private static List<Facet> populateFacets(NestedAggregate nestedAggResult, double extrapolationFactor) {
    List<Facet> facets = new ArrayList<>();

    if (nestedAggResult.aggregations().get("dynamic") != null) {
      List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("dynamic").sterms().buckets().array();
      populateAttributeFacetFromAggregationBuckets(stBuckets, facets, extrapolationFactor);
    }

    if (nestedAggResult.aggregations().get("static") != null) {
      List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("static").sterms().buckets().array();
      populateAttributeFacetFromAggregationBuckets(stBuckets, facets, extrapolationFactor);
    }

    return facets;
  }

  private static void populateAttributeFacetFromAggregationBuckets(
      List<StringTermsBucket> stBuckets, List<Facet> facets, double extrapolationFactor) {
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = Math.round(bucket.docCount() * extrapolationFactor);
      String keyKey = bucket.key().stringValue();
      Map<String, Long> facetBuckets = new HashMap<>();
      facets.add(new Facet("attr", keyKey, keyCount, facetBuckets));
      List<StringTermsBucket> valueBuckets = bucket.aggregations().get("by_value").sterms().buckets().array();
      for (StringTermsBucket valueBucket : valueBuckets) {
        long valueCount = Math.round(valueBucket.docCount() * extrapolationFactor);
        String valueKey = valueBucket.key().stringValue();
        facetBuckets.put(valueKey, valueCount);
      }
    }
  }
}
