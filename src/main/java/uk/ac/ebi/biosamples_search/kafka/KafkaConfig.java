package uk.ac.ebi.biosamples_search.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
  private final MessageConsumer messageConsumer;

  public KafkaConfig(MessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

  @Bean
  public NewTopic topic() {
    return TopicBuilder.name("test-topic")
        .partitions(10)
        .replicas(1)
        .build();
  }

  @KafkaListener(id = "myId", topics = "test-topic")
  public void listen(String in) {
    messageConsumer.processMessage(in);
  }
}
