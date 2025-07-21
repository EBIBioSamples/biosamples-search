package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record ExternalRefSearchFilter(String archive, String accession) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("externalReferences")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("externalReferences.archive.keyword")
                            .value(archive)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("externalReferences.accession.keyword")
                            .value(accession)
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
