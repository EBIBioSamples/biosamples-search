package uk.ac.ebi.biosamples.search.samples.facet;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import org.springframework.data.elasticsearch.core.SearchHits;
import uk.ac.ebi.biosamples.search.samples.Sample;

import java.util.List;
import java.util.Map;

public interface FacetingStrategy {
  Map<String, Aggregation> getDefaultAggregations();
  List<Facet> retrieveFacets(SearchHits<Sample> hits);
}
