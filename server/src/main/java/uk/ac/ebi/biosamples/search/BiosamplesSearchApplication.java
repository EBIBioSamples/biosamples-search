package uk.ac.ebi.biosamples.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication(exclude = RabbitAutoConfiguration.class)
public class BiosamplesSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(BiosamplesSearchApplication.class, args);
  }
}
