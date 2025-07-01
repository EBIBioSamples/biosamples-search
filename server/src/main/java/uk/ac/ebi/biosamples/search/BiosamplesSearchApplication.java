package uk.ac.ebi.biosamples.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BiosamplesSearchApplication {

  public static void main(String[] args) {
    SpringApplication.run(BiosamplesSearchApplication.class, args);
  }
}
