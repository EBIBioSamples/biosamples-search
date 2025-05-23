package uk.ac.ebi.biosamples_search.samples.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeSearchFilterTest {

  @Test
  void attributeSearchFilterShouldBeCorrectlyDeserialized() throws Exception {
    String serialization = """
        {
          "type": "attr",
          "field": "env_medium",
          "values": ["soil", "water"]
        }
        """;
    SearchFilter filter = new ObjectMapper().readValue(serialization, SearchFilter.class);
    assertThat(filter).isExactlyInstanceOf(AttributeSearchFilter.class);
  }
}