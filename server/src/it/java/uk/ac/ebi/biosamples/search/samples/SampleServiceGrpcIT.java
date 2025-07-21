package uk.ac.ebi.biosamples.search.samples;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.biosamples.search.IntegrationTestConfiguration;
import uk.ac.ebi.biosamples.search.TestDependencyContainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration.class)
@ImportTestcontainers(TestDependencyContainers.class)
public class SampleServiceGrpcIT {


  @Test
  void searchSamples_shouldReturnFirstAccessionPage() {

  }
}
