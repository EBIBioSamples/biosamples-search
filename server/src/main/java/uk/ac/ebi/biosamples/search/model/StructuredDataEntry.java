package uk.ac.ebi.biosamples.search.model;

import lombok.Getter;

@Getter
public class StructuredDataEntry {
  private String value;
  private String iri;

  public static StructuredDataEntry build(String value, String iri) {
    final StructuredDataEntry structuredDataEntry = new StructuredDataEntry();

    structuredDataEntry.value = value;
    structuredDataEntry.iri = iri;

    return structuredDataEntry;
  }
}
