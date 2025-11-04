package uk.ac.ebi.biosamples.search.facet;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateRangeFacet {

  public static Aggregation getAggregations() {

//    return Aggregation.of(a -> a
//        .autoDateHistogram(h -> h
//            .field("update")
//            .buckets(10)
//            .format("yyyy-MM-dd")
//        )
//    );

    return Aggregation.of(a -> a
        .dateHistogram(h -> h
            .field("update")
            .calendarInterval(CalendarInterval.Year)
            .format("yyyy-MM-dd")
        )
    );
  }

  public static List<Facet> populateFacetFromAggregationResults(ElasticsearchAggregation aggregation) {
    List<Facet> facets = new ArrayList<>();
    List<DateHistogramBucket> dateHistogramBuckets = aggregation.aggregation().getAggregate().dateHistogram().buckets().array();
//    List<DateHistogramBucket> dateHistogramBuckets = aggregation.aggregation().getAggregate().autoDateHistogram().buckets().array();

    Map<String, Long> facetBuckets = new HashMap<>();
    long docCount = 0;
    for (DateHistogramBucket bucket : dateHistogramBuckets) {
      long keyCount = bucket.docCount();
      String keyKey = bucket.keyAsString();
      facetBuckets.put(keyKey, keyCount);
      docCount += keyCount;
    }
    facets.add(new Facet("dt", "update", docCount, facetBuckets));

    return facets;
  }
}
