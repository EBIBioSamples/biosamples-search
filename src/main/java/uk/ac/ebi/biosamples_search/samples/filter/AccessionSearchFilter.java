package uk.ac.ebi.biosamples_search.samples.filter;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public final class AccessionSearchFilter implements SearchFilter {
  private final String accession;
}
