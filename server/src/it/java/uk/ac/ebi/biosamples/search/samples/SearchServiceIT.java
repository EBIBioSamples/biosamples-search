package uk.ac.ebi.biosamples.search.samples;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchPage;
import uk.ac.ebi.biosamples.search.IntegrationTestConfiguration;
import uk.ac.ebi.biosamples.search.TestDependencyContainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ImportTestcontainers(TestDependencyContainers.class)
@Import(IntegrationTestConfiguration.class)
public class SearchServiceIT {

  @Autowired
  SearchService searchService;

  @BeforeAll
  static void setUp() {

  }

  @AfterAll
  static void destroy() {

  }

  @BeforeEach
  void setupBeforeEach() {
  }

  @Test
  void searchSamples_shouldReturnPaginatedSampleList() {
    Page<Sample> samplePage = searchService.searchSamples();
    assertThat(samplePage.getTotalPages()).isGreaterThanOrEqualTo(0);
  }

  @Test
  void searchSamplesWithText_shouldReturnPaginatedSampleList() {
    SearchQuery searchQuery = SearchQuery.builder().text("SAMD00000001").build();
    SearchPage<Sample> samplePage = searchService.search(searchQuery);
    assertThat(samplePage.getTotalPages()).isEqualTo(1);
  }

}
