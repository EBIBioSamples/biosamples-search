package uk.ac.ebi.biosamples_search.samples;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import uk.ac.ebi.biosamples_search.model.*;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@Document(indexName = "samples")
//@Setting(settingPath = "/es_mappings.json")
public class Sample {
  @Id
  protected String accession;
  protected String sraAccession;
  @Field(type = FieldType.Text)
  protected String name;
  protected String domain;
  protected String webinSubmissionAccountId;
  protected Long taxId;
  protected SampleStatus status;

  @Field(type = FieldType.Date)
  protected Instant release;
  @Field(type = FieldType.Date)
  protected Instant update;
  @Field(type = FieldType.Date)
  protected Instant create;
  @Field(type = FieldType.Date)
  protected Instant submitted;

  @Field(type = FieldType.Nested)
  @JsonDeserialize(using = CharacteristicDeserializer.class)
  protected Set<Attribute> characteristics;
  @Field(type = FieldType.Nested)
  protected Set<Relationship> relationships;
  @Field(type = FieldType.Nested)
  protected Set<ExternalReference> externalReferences;
  @Field(type = FieldType.Nested)
  protected Set<Organization> organizations;
  @Field(type = FieldType.Nested)
  protected Set<Contact> contacts;
  @Field(type = FieldType.Nested)
  protected Set<Publication> publications;
  @Field(type = FieldType.Nested)
  protected Set<StructuredDataTable> structuredData;

  private SubmittedViaType submittedVia;
}
