package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public record DomainSearchFilter(String domain) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("domain.keyword")
        .value(domain)
    )._toQuery();
  }
}
