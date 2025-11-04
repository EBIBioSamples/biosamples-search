package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

import java.util.List;

public record StructuredDataSearchFilter(String dataType, String key, String value) implements SearchFilter {

  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("structuredData")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("structuredData.dataType.keyword")
                            .value(dataType)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("structuredData.key.keyword")
                            .value(key)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("structuredData.value.keyword")
                            .value(value)
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
