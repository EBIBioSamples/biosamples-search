package uk.ac.ebi.biosamples.search.index;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  public static final String INDEXING_QUEUE = "biosamples.indexing.es";
  public static final String INDEXING_EXCHANGE = "biosamples.indexing";
  public static final String REINDEXING_QUEUE = "biosamples.reindex.elasticsearch";
  public static final String REINDEXING_EXCHANGE = "biosamples.reindex.solr";
  public static final String REINDEXING_ROUTING_KEY = "biosamples.reindex.solr";

  @Bean
  Queue queue() {
    return new Queue(INDEXING_QUEUE, true);
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(INDEXING_EXCHANGE);
  }

  @Bean
  Binding binding(Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(INDEXING_QUEUE);
  }

  @Bean
  SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
                                           MessageListenerAdapter listenerAdapter) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(INDEXING_QUEUE);
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(IndexingListener listener) {
    return new MessageListenerAdapter(listener, "receiveMessage");
  }
}
