package uk.ac.ebi.biosamples.search.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Data
@Builder
public class Attribute {
  @Field(type = FieldType.Keyword)
  private String key;
  @MultiField(
      mainField = @Field(type = FieldType.Text, name = "value"),
      otherFields = {
          @InnerField(suffix = "keyword", type = FieldType.Keyword)
      }
  )
  private String value;
}
