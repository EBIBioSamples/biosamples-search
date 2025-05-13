package uk.ac.ebi.biosamples_search.samples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@Document(indexName = "samples")
public class Sample {
  @Id
  private String accession;
  @Field(type = FieldType.Text)
  private String name;
  private String description;
}
