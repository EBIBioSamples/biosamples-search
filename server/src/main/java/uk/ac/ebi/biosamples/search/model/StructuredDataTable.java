package uk.ac.ebi.biosamples.search.model;

import java.util.List;
import java.util.Map;

public record StructuredDataTable(String webinSubmissionAccountId,
                                  String type,
                                  String schema,
                                  List<Map<String, StructuredDataEntry>> content) {
}
