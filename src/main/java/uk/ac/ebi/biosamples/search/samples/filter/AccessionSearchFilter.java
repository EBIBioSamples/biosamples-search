package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public record AccessionSearchFilter(String accession) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("accession.keyword")
        .value(accession)
    )._toQuery();
  }
}
