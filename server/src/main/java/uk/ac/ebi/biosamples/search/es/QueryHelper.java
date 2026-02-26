package uk.ac.ebi.biosamples.search.es;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.filter.DateRangeSearchFilter;
import uk.ac.ebi.biosamples.search.filter.PublicSearchFilter;
import uk.ac.ebi.biosamples.search.filter.SearchFilter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryHelper {

  public static Query getSearchQuery(SearchQuery searchQuery) {
    Query filter = getFilterQuery(searchQuery);
    if (!StringUtils.hasText(searchQuery.getText())) {
      return filter;
    }
    Query match = getTextMatchQuery(searchQuery);
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

  private static final String DEFAULT_DATE_FROM = "1970-01-01T00:00:00.000Z";

  private static Query getFilterQuery(SearchQuery searchQuery) {
    if (CollectionUtils.isEmpty(searchQuery.getFilters())) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    List<SearchFilter> filters = searchQuery.getFilters();

    // Merge multiple date ranges on the same field into one (intersection: max from, min to)
    Map<DateRangeSearchFilter.DateField, Query> mergedDateRangeQueries = getMergedDateRangeQueries(filters);
    List<Query> mustQueries = new ArrayList<>(mergedDateRangeQueries.values());

    boolean hasReleaseDateRange = mergedDateRangeQueries.containsKey(DateRangeSearchFilter.DateField.RELEASE)
        || filters.stream().anyMatch(f -> f instanceof DateRangeSearchFilter dt
            && dt.field() == DateRangeSearchFilter.DateField.RELEASE);
    boolean needExcludeSuppressed = false;

    for (SearchFilter filter : filters) {
      if (filter instanceof PublicSearchFilter && hasReleaseDateRange) {
        needExcludeSuppressed = true;
        continue;
      }

      if (filter instanceof DateRangeSearchFilter dt
          && mergedDateRangeQueries.containsKey(dt.field())) {
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

  /**
   * Groups date range filters by field and merges multiple ranges on the same field
   * into one (intersection: latest from, earliest to). Returns a map of field -> a merged query
   * only for fields that had more than one filter.
   */
  private static Map<DateRangeSearchFilter.DateField, Query> getMergedDateRangeQueries(List<SearchFilter> filters) {
    final String defaultTo = Instant.now().toString();
    final Map<DateRangeSearchFilter.DateField, List<DateRangeSearchFilter>> byField = filters.stream()
        .filter(f -> f instanceof DateRangeSearchFilter)
        .map(f -> (DateRangeSearchFilter) f)
        .collect(Collectors.groupingBy(DateRangeSearchFilter::field));

    return byField.entrySet().stream()
        .filter(e -> e.getValue().size() > 1)
        .collect(Collectors.toMap(Map.Entry::getKey, e -> {
          final List<DateRangeSearchFilter> list = e.getValue();
          final String mergedFrom = list.stream()
              .map(dt -> StringUtils.hasText(dt.from()) ? dt.from() : DEFAULT_DATE_FROM)
              .max(String::compareTo)
              .orElse(DEFAULT_DATE_FROM);
          final String mergedTo = list.stream()
              .map(dt -> StringUtils.hasText(dt.to()) ? dt.to() : defaultTo)
              .min(String::compareTo)
              .orElse(defaultTo);

          return new DateRangeSearchFilter(e.getKey(), mergedFrom, mergedTo).getQuery();
        }));
  }


}
