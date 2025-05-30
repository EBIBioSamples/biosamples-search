package uk.ac.ebi.biosamples_search.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Relationship {
  private final String type;
  private final String target;
  private final String source;
}
