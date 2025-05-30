package uk.ac.ebi.biosamples_search.model;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public class StructuredDataTable {
  private String webinSubmissionAccountId;
  private String type;
  private String schema;
  private Set<Map<String, StructuredDataEntry>> content;

  public static StructuredDataTable build(
      final String webinSubmissionAccountId,
      final String type,
      final String schema,
      final Set<Map<String, StructuredDataEntry>> content) {
    final StructuredDataTable structuredDataTable = new StructuredDataTable();

    structuredDataTable.schema = schema;
    structuredDataTable.webinSubmissionAccountId = webinSubmissionAccountId;
    structuredDataTable.type = type;
    structuredDataTable.content = content;

    return structuredDataTable;
  }
}
