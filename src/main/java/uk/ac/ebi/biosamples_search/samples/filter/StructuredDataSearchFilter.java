package uk.ac.ebi.biosamples_search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public final class StructuredDataSearchFilter implements SearchFilter {
  private final String type;
  private final String field;
  private final String value;

  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("structuredData")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("structuredData.type.keyword")
                            .value(field)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("structuredData.field.keyword")
                            .value(field)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("structuredData.value.keyword")
                            .value(field)
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
