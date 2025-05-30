package uk.ac.ebi.biosamples_search.model;

import java.util.Arrays;
import java.util.List;

public enum SampleStatus {
  DRAFT,
  PRIVATE,
  PUBLIC,
  CANCELLED,
  SUPPRESSED,
  KILLED;

  public static List<String> getSearchHiddenStatuses() {
    return Arrays.asList(SUPPRESSED.name(), KILLED.name());
  }
}
