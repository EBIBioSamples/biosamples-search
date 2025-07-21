package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record RelationshipSearchFilter(String relType, String source, String target) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    return NestedQuery.of(n -> n
        .path("relationships")
        .query(q -> q
            .bool(b -> b
                .must(
                    List.of(
                        TermQuery.of(t -> t
                            .field("relationships.type.keyword")
                            .value(relType)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("relationships.source.keyword")
                            .value(source)
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("relationships.target.keyword")
                            .value(target)
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
