package uk.ac.ebi.biosamples.search.samples;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.data.domain.Page;
import uk.ac.ebi.biosamples.search.TestDependencyContainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ImportTestcontainers(TestDependencyContainers.class)
public class SearchServiceIT {

  @Autowired
  SearchService searchService;

  @BeforeAll
  static void setUp() {

  }

  @AfterAll
  static void destroy() {

  }

  @Test
  void searchSamples_shouldReturnPaginatedSampleList() {
    Page<Sample> samplePage = searchService.searchSamples();
    assertThat(samplePage.getTotalPages()).isGreaterThanOrEqualTo(0);
  }

}
