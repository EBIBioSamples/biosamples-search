package uk.ac.ebi.biosamples.search.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Organization {
  private final String name;
  private final String role;
  private final String address;
  private final String email;
  private final String url;
}
