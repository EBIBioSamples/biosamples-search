package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public record WebinIdSearchFilter(String webinId) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("webinSubmissionAccountId.keyword")
        .value(webinId)
    )._toQuery();
  }
}
