package uk.ac.ebi.biosamples_search.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public enum StructuredDataType {
  AMR(Collections.emptyList()),
  CHICKEN_DATA(Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method")),
  HISTOLOGY_MARKERS(
      Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method")),
  MOLECULAR_MARKERS(
      Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method")),
  FATTY_ACIDS(Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method")),
  HEAVY_METALS(Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method")),
  SALMON_DATA(Arrays.asList("Marker", "Measurement", "Measurement Units", "Partner", "Method"));

  private final List<String> headers;

  StructuredDataType(List<String> headers) {
    this.headers = headers;
  }
}
