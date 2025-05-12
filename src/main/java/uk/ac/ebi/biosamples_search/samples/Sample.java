package uk.ac.ebi.biosamples_search.samples;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Sample {
  @Id
  private String accession;
  private String name;
  private String description;
}
