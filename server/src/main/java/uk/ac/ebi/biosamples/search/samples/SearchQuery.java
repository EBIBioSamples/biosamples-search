package uk.ac.ebi.biosamples.search.samples;

import lombok.Builder;
import lombok.Getter;
import uk.ac.ebi.biosamples.search.samples.filter.SearchFilter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class SearchQuery {
  private final String text;
  private final List<SearchFilter> filters;

  private final Integer page;
  private final Integer size;
  private final List<SortOrder> sort; //allways add update + accession
  private final List<String> searchAfter;

//  private final String scrollId;
//  private final String scrollAfter;
//  private final List<String> facets;
}
