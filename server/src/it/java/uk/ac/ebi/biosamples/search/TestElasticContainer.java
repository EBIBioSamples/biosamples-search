package uk.ac.ebi.biosamples.search;

import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class TestElasticContainer extends ElasticsearchContainer {

  public TestElasticContainer(String imageName) {
    super(imageName);
    addEnv("xpack.security.enabled", "false");
    addEnv("xpack.security.enrollment.enabled", "false");
  }
}
