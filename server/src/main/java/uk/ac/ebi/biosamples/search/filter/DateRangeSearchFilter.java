package uk.ac.ebi.biosamples.search.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.DateRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.util.StringUtils;

import java.time.Instant;

public record DateRangeSearchFilter(DateField field, String from, String to) implements SearchFilter {

  /** Sentinel "end of time" sent by clients when "to" is omitted; ES cannot parse it. Treat as "no end" → now. */
  private static final String SENTINEL_FAR_FUTURE_PREFIX = "+999999999";

  @JsonIgnore
  public Query getQuery() {
    //todo from, to validation "2023-05-12T15:12:56.113Z"

    DateRangeQuery.Builder builder = new DateRangeQuery.Builder().field(field.name().toLowerCase());
    if (StringUtils.hasText(from)) {
      builder.gte(from);
    } else {
      builder.gte("1970-01-01T00:00:00.000Z");
    }

    String effectiveTo = effectiveTo();
    
    if (StringUtils.hasText(effectiveTo)) {
      builder.lte(effectiveTo);
    } else {
      builder.lte(Instant.now().toString());
    }

    return RangeQuery.of(r -> r
        .date(builder.build())
    )._toQuery();
  }

  /** Use {@code to} unless it is the far-future sentinel (e.g. +999999999-12-31...); then treat as no end. */
  private String effectiveTo() {
    if (!StringUtils.hasText(to)) {
      return null;
    }

    if (to.startsWith(SENTINEL_FAR_FUTURE_PREFIX)) {
      return null;
    }
    
    return to;
  }

  public enum DateField {
    RELEASE, UPDATE, SUBMITTED, CREATE;

    @JsonValue
    public String toJson() {
      return name().toLowerCase();
    }
  }
}
