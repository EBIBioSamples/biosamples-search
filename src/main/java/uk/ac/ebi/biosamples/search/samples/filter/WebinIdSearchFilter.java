package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record WebinIdSearchFilter(String webinId) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("webinSubmissionAccountId.keyword")
        .value(webinId)
    )._toQuery();
  }
}
