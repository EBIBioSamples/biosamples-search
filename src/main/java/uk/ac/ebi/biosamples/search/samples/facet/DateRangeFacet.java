package uk.ac.ebi.biosamples.search.samples.facet;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateRangeFacet {

  public static Aggregation getAggregations() {
    return Aggregation.of(a -> a
        .dateHistogram(h -> h
            .field("update")
            .calendarInterval(CalendarInterval.Year)
            .format("yyyy")
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    List<Facet> facets = new ArrayList<>();
    List<DateHistogramBucket> dateHistogramBuckets = aggregation.aggregation().getAggregate().dateHistogram().buckets().array();
    Map<String, Long> facetBuckets = new HashMap<>();
    facets.add(new Facet("dt", "update", -1, facetBuckets));

    for (DateHistogramBucket bucket : dateHistogramBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.keyAsString();
      facetBuckets.put(keyKey, keyCount);
    }

    return facets;
  }
}
