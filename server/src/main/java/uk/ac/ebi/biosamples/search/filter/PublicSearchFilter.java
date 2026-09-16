package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

public record PublicSearchFilter(String webinId) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    Query publicDateQuery = new DateRangeSearchFilter(
        DateRangeSearchFilter.DateField.RELEASE, null, Instant.now().toString()).getQuery();

    Query suppressedStatusQuery = getExcludeSuppressedQuery();

    Query publicQuery = new BoolQuery.Builder()
        .must(List.of(publicDateQuery))
        .mustNot(List.of(suppressedStatusQuery))
        .build()
        ._toQuery();

    if (StringUtils.hasText(webinId)) {
      Query authQuery = new WebinIdSearchFilter(webinId).getQuery();
      return new BoolQuery.Builder().should(List.of(authQuery, publicQuery)).build()._toQuery();
    }

    return publicQuery;
  }

  /**
   * Returns the "exclude suppressed" query only (must_not for INSDC status=suppressed).
   * Used when combining with a release date range so we can avoid a redundant 1970→now range.
   */
  public static Query getExcludeSuppressedQuery() {
    return new AttributeSearchFilter("INSDC status", List.of("suppressed")).getQuery();
  }
}
