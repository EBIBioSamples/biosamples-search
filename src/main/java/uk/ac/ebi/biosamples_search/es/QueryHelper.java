package uk.ac.ebi.biosamples_search.es;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import uk.ac.ebi.biosamples_search.samples.SearchQuery;
import uk.ac.ebi.biosamples_search.samples.filter.SearchFilter;

import java.util.List;

public class QueryHelper {

  public static Query getSearchQuery(SearchQuery searchQuery) {
    Query match = getTextMatchQuery(searchQuery);
    Query filter = getFilterQuery(searchQuery);
    return BoolQuery.of(b -> b
        .must(match)
        .filter(filter)
    )._toQuery();
  }

  private static Query getTextMatchQuery(SearchQuery searchQuery) {
    String searchText = searchQuery.getText();
    return MatchQuery.of(m -> m
        .field("sample_full_text")
        .query(searchText)
    )._toQuery();
  }

  private static Query getFilterQuery(SearchQuery searchQuery) {
    List<Query> filterQueries = searchQuery.getFilters().stream()
        .map(SearchFilter::getQuery).toList();
    if (filterQueries.isEmpty()) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }
    return BoolQuery.of(b -> b.must(filterQueries))._toQuery();
  }


}
