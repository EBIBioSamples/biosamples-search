package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

public record AttributeSearchFilter(String field, List<String> values) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("characteristics")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("characteristics.key.keyword")
                            .value(field)
                        )._toQuery(),
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
                        TermQuery.of(t -> t
                            .field("characteristics.value.keyword")
                            .value(v)
                        )._toQuery())
                    .toList()
            )
        )
    );
  }
}

