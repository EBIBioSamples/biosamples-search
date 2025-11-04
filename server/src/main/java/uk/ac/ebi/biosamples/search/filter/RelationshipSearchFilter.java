package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record RelationshipSearchFilter(String relType, String source, String target) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    List<Query> queries = new ArrayList<>();
    if (StringUtils.hasText(relType)) {
      queries.add(
          TermQuery.of(t -> t
              .field("relationships.type.keyword")
              .value(relType)
          )._toQuery());
    }
    if (StringUtils.hasText(source)) {
      queries.add(TermQuery.of(t -> t
          .field("relationships.source.keyword")
          .value(source)
      )._toQuery());
    }
    if (StringUtils.hasText(target)) {
      queries.add(
          TermQuery.of(t -> t
              .field("relationships.target.keyword")
              .value(target)
          )._toQuery());
    }


    return NestedQuery.of(n -> n
        .path("relationships")
        .query(q -> q.bool(b -> b.must(queries)))
    )._toQuery();
  }
}
