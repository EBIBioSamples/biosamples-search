package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record StructuredDataSearchFilter(String dataType, String key, String value) implements SearchFilter {

  public Query getQuery() {
    List<Query> queries = new ArrayList<>();
    
    // dataType is required
    if (StringUtils.hasText(dataType)) {
      queries.add(TermQuery.of(t -> t
          .field("structuredData.dataType.keyword")
          .value(dataType)
      )._toQuery());
    }
    
    // key is optional
    if (StringUtils.hasText(key)) {
      queries.add(TermQuery.of(t -> t
          .field("structuredData.key.keyword")
          .value(key)
      )._toQuery());
    }
    
    // value is optional
    if (StringUtils.hasText(value)) {
      queries.add(TermQuery.of(t -> t
          .field("structuredData.value.keyword")
          .value(value)
      )._toQuery());
    }
    
    return NestedQuery.of(n -> n
        .path("structuredData")
        .query(q -> q
            .bool(b -> b
                .must(queries)
            )
        )
    )._toQuery();
  }
}
