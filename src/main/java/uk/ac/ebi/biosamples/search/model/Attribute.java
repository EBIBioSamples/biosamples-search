package uk.ac.ebi.biosamples.search.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Builder
public class Attribute {
  @Field(type = FieldType.Keyword)
  private String key;
//  @Field(type = FieldType.Keyword)
  @MultiField(
      mainField = @Field(type = FieldType.Text, name = "value"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String value;
}
