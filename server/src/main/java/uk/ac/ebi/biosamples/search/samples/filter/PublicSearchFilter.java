package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

public record PublicSearchFilter(String webinId) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    Query publicDateQuery = new DateRangeSearchFilter(
        DateRangeSearchFilter.DateField.RELEASE, null, Instant.now().toString()).getQuery();

    Query suppressedStatusQuery =
        new AttributeSearchFilter("INSDC status", List.of("suppressed")).getQuery();

    Query publicQuery = new BoolQuery.Builder()
        .must(List.of(publicDateQuery, getSurpressedStatusQuery()))
//        .mustNot(List.of(suppressedStatusQuery))
        .build()
        ._toQuery();

    if (StringUtils.hasText(webinId)) {
      Query authQuery = new WebinIdSearchFilter(webinId).getQuery();
      return new BoolQuery.Builder().should(List.of(authQuery, publicQuery)).build()._toQuery();
    }

    return publicQuery;
  }

  public Query getSurpressedStatusQuery() {
    return NestedQuery.of(n -> n
        .path("characteristics")
        .query(q -> q
            .bool(b -> b
                .mustNot(
                    List.of(
                        TermQuery.of(t -> t
                            .field("characteristics.key.keyword")
                            .value("INSDC status")
                        )._toQuery(),
                        TermQuery.of(t -> t
                            .field("characteristics.value.keyword")
                            .value("suppressed")
                        )._toQuery()
                    )
                )
            )
        )
    )._toQuery();
  }
}
