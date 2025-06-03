package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public record NameSearchFilter(String name) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("name.keyword")
        .value(name)
    )._toQuery();
  }
}
