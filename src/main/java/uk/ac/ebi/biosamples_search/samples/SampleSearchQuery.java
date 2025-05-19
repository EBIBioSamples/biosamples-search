package uk.ac.ebi.biosamples_search.samples;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SampleSearchQuery {
  private final String text;
  private final String[] filter;
  private final String cursor;
  private final Integer page;
  private final Integer size;
  private final String[] sort;
  private final boolean curate;
}
