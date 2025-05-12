package uk.ac.ebi.biosamples_search.samples;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SamplesController {

  @GetMapping("/test")
  public String postTestMessage() {
    return "works ok";
  }

  @GetMapping("/search")
  public Page<Sample> searchSamples() {
    //TODO
    return null;
  }

}
