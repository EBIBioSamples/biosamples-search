package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;

public record AccessionSearchFilter(String accession) implements SearchFilter {

  /** Index mapping: accession is text with fields.keyword (ignore_above 256). Use keyword subfield for term/wildcard. */
  private static final String ACCESSION_KEYWORD_FIELD = "accession.keyword";

  public Query getQuery() {
    if (isWildcardPattern(accession)) {
      return WildcardQuery.of(w -> w
          .field(ACCESSION_KEYWORD_FIELD)
          .value(toElasticsearchWildcard(accession))
      )._toQuery();
    }
    return TermQuery.of(t -> t
        .field(ACCESSION_KEYWORD_FIELD)
        .value(accession)
    )._toQuery();
  }

  /**
   * Converts regex-style .* to ES wildcard * (literal dot in ES wildcard would break SAME.* matching SAMEA1).
   */
  private static String toElasticsearchWildcard(String value) {
    return value == null ? null : value.replace(".*", "*");
  }

  private static boolean isWildcardPattern(String value) {
    return value != null && (value.indexOf('*') >= 0 || value.indexOf('?') >= 0);
  }
}
