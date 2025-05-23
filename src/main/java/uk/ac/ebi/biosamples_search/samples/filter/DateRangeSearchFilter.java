package uk.ac.ebi.biosamples_search.samples.filter;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public final class DateRangeSearchFilter implements SearchFilter {
  private final String field;
  private final String from;
  private final String to;

//  release: "2014-04-07T00:00:00Z",
//  update: "2023-05-12T15:12:56.113Z",
//  submitted: "2014-04-07T00:00:00Z",
//  create: "2014-04-07T00:00:00Z",
}
