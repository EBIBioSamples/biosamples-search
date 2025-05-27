package uk.ac.ebi.biosamples_search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public final class RelationshipSearchFilter implements SearchFilter {
  private final String relType;
  private final String source;
  private final String target;

  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("relationships")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("relationships.relType.keyword")
                            .value(relType)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("relationships.source.keyword")
                            .value(relType)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("relationships.target.keyword")
                            .value(relType)
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
