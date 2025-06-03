package uk.ac.ebi.biosamples.search.samples.facet;

import java.util.Map;

public record Facet(String type, String field, long count, Map<String, Long> buckets) {
  // first request from static facets, then if only few request dynamic from characteristics

  // characteristics
  // date
  // relationships
  // external ref
}
