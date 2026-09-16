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
  public static final String INDEXING_EXCHANGE = "biosamples.indexing";
  public static final String REINDEXING_EXCHANGE = "biosamples.reindexing";
  public static final String INDEXING_QUEUE = "biosamples.indexing.es";
  public static final String REINDEXING_QUEUE = "biosamples.reindexing.es";

  @Bean
  Queue queue() {
    return new Queue(INDEXING_QUEUE, true);
  }

  @Bean
  Queue reindexingQueue() {
    return new Queue(REINDEXING_QUEUE, true);
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(INDEXING_EXCHANGE);
  }

  @Bean
  DirectExchange reindexingExchange() {
    return new DirectExchange(REINDEXING_EXCHANGE);
  }

  @Bean
  Binding reindexingBinding(Queue reindexingQueue, DirectExchange reindexingExchange) {
    return BindingBuilder.bind(reindexingQueue).to(reindexingExchange).with(REINDEXING_QUEUE);
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
    container.setQueueNames(INDEXING_QUEUE, REINDEXING_QUEUE);
    container.setMessageListener(listenerAdapter);
    return container;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(IndexingListener listener) {
    return new MessageListenerAdapter(listener, "receiveMessage");
  }
}
