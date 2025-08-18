package uk.ac.ebi.biosamples.search.samples.facet;

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
            .terms(t -> t.field("externalReferences.archive.keyword"))
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    List<Facet> facets = new ArrayList<>();
    NestedAggregate nestedAggResult = aggregation.aggregation().getAggregate().nested();

    Map<String, Long> facetBuckets = new HashMap<>();
    facets.add(new Facet("extd", "external ref", nestedAggResult.docCount(), facetBuckets));
    List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_archive").sterms().buckets().array();
    for (StringTermsBucket bucket : stBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.key().stringValue();
      facetBuckets.put(keyKey, keyCount);
    }

    return facets;
  }
}
