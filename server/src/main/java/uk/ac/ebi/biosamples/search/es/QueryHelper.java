package uk.ac.ebi.biosamples.search.es;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.filter.DateRangeSearchFilter;
import uk.ac.ebi.biosamples.search.filter.PublicSearchFilter;
import uk.ac.ebi.biosamples.search.filter.SearchFilter;

import java.util.ArrayList;
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

    if (!StringUtils.hasText(searchText)) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    if (searchText.startsWith("\"") && searchText.endsWith("\"")) {
      String searchPhrase = searchText.substring(1, searchText.length() - 1);
      return MatchPhraseQuery.of(m -> m.field("sample_full_text").query(searchPhrase))._toQuery();
    }

//    return MatchQuery.of(m -> m.field("sample_full_text").query(searchText))._toQuery();

    return QueryStringQuery.of(qs -> qs
        .defaultField("sample_full_text")
        .query(searchText)
        .defaultOperator(Operator.Or) // Default to OR if no operator is specified by the user
    )._toQuery();

  }

  private static Query getFilterQuery(SearchQuery searchQuery) {
    if (CollectionUtils.isEmpty(searchQuery.getFilters())) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    List<SearchFilter> filters = searchQuery.getFilters();
    boolean hasReleaseDateRange = filters.stream()
        .anyMatch(f -> f instanceof DateRangeSearchFilter dt
            && dt.field() == DateRangeSearchFilter.DateField.RELEASE);

    List<Query> mustQueries = new ArrayList<>();
    boolean needExcludeSuppressed = false;

    for (SearchFilter filter : filters) {
      if (filter instanceof PublicSearchFilter && hasReleaseDateRange) {
        needExcludeSuppressed = true;
        continue;
      }
      mustQueries.add(filter.getQuery());
    }

    if (needExcludeSuppressed) {
      return BoolQuery.of(b -> b
          .must(mustQueries)
          .mustNot(PublicSearchFilter.getExcludeSuppressedQuery())
      )._toQuery();
    }
    return BoolQuery.of(b -> b.must(mustQueries))._toQuery();
  }


}
