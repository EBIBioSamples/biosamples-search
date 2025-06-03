package uk.ac.ebi.biosamples.search.model;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public class Contact {
  private final String firstName;
  private final String lastName;
  private final String midInitials;
  private final String role;
  private final String email;
  private final String affiliation;
  private final String name;
  private final String url;
}
