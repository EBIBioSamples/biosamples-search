package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public final class ExternalRefSearchFilter implements SearchFilter {
  private final String archive;
  private final String accession;

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
