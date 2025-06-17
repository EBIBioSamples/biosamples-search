package uk.ac.ebi.biosamples.search.es;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.samples.filter.SearchFilter;

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
    return StringUtils.hasText(searchText) ?
        MatchQuery.of(m -> m.field("sample_full_text").query(searchText))._toQuery() :
        MatchAllQuery.of(m -> m)._toQuery();
  }

  private static Query getFilterQuery(SearchQuery searchQuery) {
    if (CollectionUtils.isEmpty(searchQuery.getFilters())) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    List<Query> filterQueries = searchQuery.getFilters().stream()
        .map(SearchFilter::getQuery).toList();
    return BoolQuery.of(b -> b.must(filterQueries))._toQuery();
  }


}
