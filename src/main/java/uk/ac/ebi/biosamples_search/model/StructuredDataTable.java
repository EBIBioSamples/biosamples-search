/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
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
