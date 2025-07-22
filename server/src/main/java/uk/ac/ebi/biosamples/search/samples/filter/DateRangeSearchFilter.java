package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.util.StringUtils;

public record DateRangeSearchFilter(DateField field, String from, String to) implements SearchFilter {

  @JsonIgnore
  public Query getQuery() {
    //todo from, to validation "2023-05-12T15:12:56.113Z"

    DateRangeQuery.Builder builder = new DateRangeQuery.Builder().field(field.name().toLowerCase());
    if (StringUtils.hasText(from)) {
      builder.gte(from);
    }
    if (StringUtils.hasText(to)) {
      builder.lte(to);
    }

    return RangeQuery.of(r -> r
        .date(builder.build())
    )._toQuery();
  }

  public enum DateField {
    RELEASE, UPDATE, SUBMITTED, CREATE;

    @JsonValue
    public String toJson() {
      return name().toLowerCase();
    }
  }
}
