package uk.ac.ebi.biosamples_search.kafka;

import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {
  public void processMessage(String message) {

    System.out.println(message);
  }
}
