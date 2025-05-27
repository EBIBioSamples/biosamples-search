package uk.ac.ebi.biosamples_search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public record SraAccessionSearchFilter(String accession) implements SearchFilter {

  public Query getQuery() {
    return TermQuery.of(t -> t
        .field("sraAccession.keyword")
        .value(accession)
    )._toQuery();
  }
}
