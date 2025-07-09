package uk.ac.ebi.biosamples.search;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ebi.biosamples.search.TestDependencyContainers.elasticContainer;
import static uk.ac.ebi.biosamples.search.TestDependencyContainers.rabbitContainer;

@SpringBootTest
@ImportTestcontainers(TestDependencyContainers.class)
public class BioSamplesSearchApplicationIT {

  @Test
  void whenAllDependenciesAreUp_thenSpringContextLoads() {
    assertThat(elasticContainer.isRunning()).isTrue();
    assertThat(rabbitContainer.isRunning()).isTrue();
  }
}
