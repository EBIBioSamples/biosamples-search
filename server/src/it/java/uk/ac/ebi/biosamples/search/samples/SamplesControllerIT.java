package uk.ac.ebi.biosamples.search.samples;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.PagedModel;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.biosamples.search.IntegrationTestConfiguration;
import uk.ac.ebi.biosamples.search.TestDependencyContainers;
import uk.ac.ebi.biosamples.search.samples.filter.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(TestDependencyContainers.class)
@Import(IntegrationTestConfiguration.class)
public class SamplesControllerIT {
  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void searchSamplesWithEmptyRequestBody_shouldReturnPaginatedListOfAllSamples() {
    String url = "http://localhost:" + port + "/search";
    SearchQuery searchQuery = SearchQuery.builder().build();
    PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
    assertThat(response).isNotNull();
    assertThat(response.getMetadata().getTotalElements()).isGreaterThanOrEqualTo(700);
  }

  @Test
  void facetRequestWithEmptyRequestBody_shouldReturnFacetsOfAllSamples() {
    String url = "http://localhost:" + port + "/facet";
    SearchQuery searchQuery = SearchQuery.builder().build();
    String response = restTemplate.postForObject(url, searchQuery, String.class);
    assertThat(response).isNotNull();
  }

  @Test
  void searchSamplesWithTextSearch_shouldReturnPaginatedListOfAllSamples() {
    String url = "http://localhost:" + port + "/search";
    SearchQuery searchQuery = SearchQuery.builder().text("symbiont").build();
    PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
    assertThat(response).isNotNull();
    assertThat(response.getMetadata().getTotalElements()).isEqualTo(2);
  }

  @Test
  void paginatingThroughSamples_shouldReturnAllPages() {
    String url = "http://localhost:" + port + "/search";
    SearchQuery searchQuery = SearchQuery.builder().size(100).build();
    PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
    int page = 0;
    while(!CollectionUtils.isEmpty(response.getContent())) {
      page++;
      searchQuery = searchQuery.toBuilder().page(page).build();
      response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
    }
    assertThat(page).isGreaterThanOrEqualTo(7);
  }

  @Nested
  class SearchFilterIT {

    @Test
    void searchSamplesWithAccessionFilter_shouldReturnTheSampleWithAccession() {
      String accession = "SAMD00000001";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder().filters(List.of(new AccessionSearchFilter(accession))).build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(accession);
    }

    @Test
    void searchSamplesWithSraAccessionFilter_shouldReturnTheSampleWithSraAccession() {
      String accession = "ERS23082093";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder().filters(List.of(new SraAccessionSearchFilter(accession))).build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(accession);
    }

    @Test
    void searchSamplesWithNameFilter_shouldReturnAllSamplesWithTheGivenName() {
      String name = "Integration Test Name Filter";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder().filters(List.of(new NameSearchFilter(name))).build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(2);
      assertThat(response.getContent().stream().findFirst().toString()).contains(name);
    }

    @Test
    void searchSamplesWithWebinFilter_shouldReturnAllSamplesWithThatWebinId() {
      String webinId = "Webin-40894";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder().filters(List.of(new WebinIdSearchFilter(webinId))).build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(41);
      assertThat(response.getContent().stream().findFirst().toString()).contains(webinId);
    }

    @Test
    void searchSamplesWithDomainFilter_shouldReturnAllSamplesWithThatDomain() {
      String domain = "self.BiosampleImportNCBI";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder().filters(List.of(new DomainSearchFilter(domain))).build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(472);
      assertThat(response.getContent().stream().findFirst().toString()).contains(domain);
    }

    @Test
    void searchSamplesWithDateRangeFilter_shouldReturnAllSamplesWithinGivenDateRange() {
      DateRangeSearchFilter.DateField field = DateRangeSearchFilter.DateField.CREATE;
      String from = "2015-09-16T16:20:02Z";
      String to = "2015-09-16T16:22:02Z";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder()
          .filters(List.of(new DateRangeSearchFilter(field, from, to)))
          .build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(3);
      assertThat(response.getContent().stream().findFirst().toString()).contains("SAMD00000358");
    }

    @Test
    void searchSamplesWithAttributeFilter_shouldReturnAllSamplesWithinGivenAttribute() {
      String field = "health_disease_stat";
      List<String> values = List.of("hemolytic jaundice");
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder()
          .filters(List.of(new AttributeSearchFilter(field, values)))
          .build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(field);
    }

    @Test
    void searchSamplesWithRelationshipFilter_shouldReturnAllSamplesWithinGivenRelationship() { // todo reverse
      String relType = "derived_from";
      String source = "SAMD00061034";
      String target = "SAMD00018432";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder()
          .filters(List.of(new RelationshipSearchFilter(relType, source, target)))
          .build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(source);
    }

    @Test
    void searchSamplesWithExternalRefFilter_shouldReturnAllSamplesWithinGivenExternalRef() {
      String archive = "ENA";
      String accession = "SAMD00117325";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder()
          .filters(List.of(new ExternalRefSearchFilter(archive, accession)))
          .build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(accession);
    }

    @Test
    void searchSamplesWithStructuredDataFilter_shouldReturnAllSamplesWithinGivenStructuredData() {
      String type = "SAMPLE";
      String field = "marker";
      String value = "Index PCR cycles";
      String url = "http://localhost:" + port + "/search";
      SearchQuery searchQuery = SearchQuery.builder()
          .filters(List.of(new StructuredDataSearchFilter(type, field, value)))
          .build();
      PagedModel<?> response = restTemplate.postForObject(url, searchQuery, PagedModel.class);
      assertThat(response.getMetadata().getTotalElements()).isEqualTo(1);
      assertThat(response.getContent().stream().findFirst().toString()).contains(value);
    }

  }
}
