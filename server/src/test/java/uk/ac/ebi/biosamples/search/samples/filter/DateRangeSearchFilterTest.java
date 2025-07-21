package uk.ac.ebi.biosamples.search.samples.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DateRangeSearchFilterTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void DateRangeSearchFilter_shouldSerialiseCorrectly() throws Exception {
    DateRangeSearchFilter filter = new DateRangeSearchFilter(DateRangeSearchFilter.DateField.CREATE, "2014-04-07T00:00:00Z", "2025-04-07T00:00:00Z");

    String filterSerialised = objectMapper.writeValueAsString(filter);

    assertThat(filterSerialised).contains("create");
    assertThat(filterSerialised).contains("2014-04-07");
  }

}