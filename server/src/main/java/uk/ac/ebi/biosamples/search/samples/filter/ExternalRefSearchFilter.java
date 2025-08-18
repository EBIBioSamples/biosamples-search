package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public record ExternalRefSearchFilter(String archive, String accession) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    List<Query> queries = new ArrayList<>();
    if (StringUtils.hasText(archive)) {
      queries.add(
          TermQuery.of(t -> t
              .field("externalReferences.archive.keyword")
              .value(archive)
          )._toQuery());
    }
    if (StringUtils.hasText(accession)) {
      queries.add(
          TermQuery.of(t -> t
              .field("externalReferences.accession.keyword")
              .value(accession)
          )._toQuery());
    }
    return NestedQuery.of(n -> n
        .path("externalReferences")
        .query(q -> q
            .bool(b -> b
                .must(queries)
            )
        )
    )._toQuery();
  }
}
