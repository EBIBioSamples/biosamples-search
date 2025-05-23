package uk.ac.ebi.biosamples_search.samples.filter;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public final class RelationshipSearchFilter implements SearchFilter {
  private final String source;
  private final String target;
  private final String relType;
}
