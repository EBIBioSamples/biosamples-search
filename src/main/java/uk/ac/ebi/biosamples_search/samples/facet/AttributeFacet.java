package uk.ac.ebi.biosamples_search.samples.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeFacet {
  private static final List<String> EXCLUDED_FACETS = List.of("sample name", "title", "sample comment", "INSDC first public", "INSDC secondary accession", "INSDC last update", "collection date", "SRA accession", "External Id");
  private static final List<String> STATIC_FACETS = List.of("organism", "sex");

  public static Aggregation getAggregations() {
    return Aggregation.of(a -> a
        .nested(n -> n.path("characteristics"))
        .aggregations("by_key", a1 -> a1
            .terms(t -> t
                .field("characteristics.key.keyword")
                .exclude(e -> e.terms(EXCLUDED_FACETS))
                .size(10)
            )
            .aggregations("by_value", a2 -> a2
                .terms(t2 -> t2
                    .field("characteristics.value.keyword")
                )
            )
        )
        .aggregations("static", a1 -> a1
            .terms(t -> t
                .field("characteristics.key.keyword")
                .include(i -> i.terms(STATIC_FACETS))
            )
            .aggregations("by_value", a2 -> a2
                .terms(t2 -> t2
                    .field("characteristics.value.keyword")
                )
            )
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    List<Facet> facets = new ArrayList<>();
    NestedAggregate nestedAggResult = aggregation.aggregation().getAggregate().nested();
    List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_key").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      Map<String, Long> facetBuckets = new HashMap<>();
      facets.add(new Facet("attr", keyKey, keyCount, facetBuckets));
      List<StringTermsBucket> valueBuckets = bucket.aggregations().get("by_value").sterms().buckets().array();
      for (StringTermsBucket valueBucket : valueBuckets) {
        long valueCount = valueBucket.docCount();
        String valueKey = valueBucket.key().stringValue();
        facetBuckets.put(valueKey, valueCount);
      }
    }


    stBuckets = nestedAggResult.aggregations().get("static").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      Map<String, Long> facetBuckets = new HashMap<>();
      facets.add(new Facet("attr", keyKey, keyCount, facetBuckets));
      List<StringTermsBucket> valueBuckets = bucket.aggregations().get("by_value").sterms().buckets().array();
      for (StringTermsBucket valueBucket : valueBuckets) {
        long valueCount = valueBucket.docCount();
        String valueKey = valueBucket.key().stringValue();
        facetBuckets.put(valueKey, valueCount);
      }
    }

    return facets;
  }
}
