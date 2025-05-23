package uk.ac.ebi.biosamples_search.samples;

import lombok.Builder;
import lombok.Getter;
import uk.ac.ebi.biosamples_search.samples.filter.SearchFilter;

import java.util.List;

@Getter
@Builder
public class SearchQuery {
  private final String text;
  private final List<SearchFilter> filters;

  private final String cursor;
  private final Integer page;
  private final Integer size;
  private final String[] sort;
}
