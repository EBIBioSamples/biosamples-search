package uk.ac.ebi.biosamples_search.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Publication {
  private final String doi;
  private final String pubmed_id;
}
