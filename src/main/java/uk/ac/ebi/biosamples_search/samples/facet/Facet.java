package uk.ac.ebi.biosamples_search.samples.facet;

import java.util.Map;

public record Facet(String filter, String field, long count, Map<String, Long> buckets) {
}
