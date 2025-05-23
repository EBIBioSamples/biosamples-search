package uk.ac.ebi.biosamples_search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Getter
@Jacksonized
public final class AttributeSearchFilter implements SearchFilter {
  private final String field;
  private final List<String> values;

  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("characteristics")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t.field("characteristics.key.keyword").value(field))._toQuery(),
                        buildSubQueryForOrCondition()
                    )
                )
            )
        )
    )._toQuery();
  }

  private Query buildSubQueryForOrCondition() {
    return Query.of(q -> q
        .bool(b -> b
            .should(
                values.stream()
                    .map(v ->
                        TermQuery.of(t -> t.field("characteristics.value.keyword").value(v))._toQuery())
                    .toList()
            )
        )
    );
  }
}

