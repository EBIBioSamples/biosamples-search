package uk.ac.ebi.biosamples_search.samples;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SearchFilter {
  private final String type;
  private final String field;
  private final String value;
}
