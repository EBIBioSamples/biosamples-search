package uk.ac.ebi.biosamples.search;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;

public interface TestDependencyContainers {
  String ELASTIC_IMAGE = "elasticsearch:9.0.2";
  String RABBIT_IMAGE = "rabbitmq:3.10.7-management-alpine";

  @Container
  @ServiceConnection
  ElasticsearchContainer elasticContainer = new TestElasticContainer(ELASTIC_IMAGE);
  @Container
  @ServiceConnection
  RabbitMQContainer rabbitContainer = new RabbitMQContainer(RABBIT_IMAGE);
}
