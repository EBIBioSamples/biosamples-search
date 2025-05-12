package uk.ac.ebi.biosamples_search.samples;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.biosamples_search.kafka.MessageProducer;

import java.util.Collection;

@RestController
public class SamplesController {
  private final MessageProducer messageProducer;

  public SamplesController(MessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  @GetMapping("/test")
  public void postTestMessage() {
    messageProducer.sendMessage();
  }

  @GetMapping("/search")
  public Page<Sample> searchSamples() {
    //TODO
    return null;
  }

}
