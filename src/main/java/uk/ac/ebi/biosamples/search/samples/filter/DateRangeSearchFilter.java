package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public final class DateRangeSearchFilter implements SearchFilter {
  private final DateField field;
  private final String from;
  private final String to;

  public Query getQuery() {
    //todo from, to validation
    return RangeQuery.of(r -> r
        .date(d -> d
            .field(field.name().toLowerCase())
            .gte(from)
            .lte(to))
    )._toQuery();
  }

  //  release: "2014-04-07T00:00:00Z",
//  update: "2023-05-12T15:12:56.113Z",
//  submitted: "2014-04-07T00:00:00Z",
//  create: "2014-04-07T00:00:00Z",
  enum DateField {
    RELEASE, UPDATE, SUBMITTED, CREATE;

    @JsonValue
    public String toJson() {
      return name().toLowerCase();
    }
  }
}
