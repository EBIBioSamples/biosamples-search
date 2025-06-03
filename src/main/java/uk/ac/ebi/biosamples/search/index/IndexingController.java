package uk.ac.ebi.biosamples.search.index;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexingController {
  private final IndexingService indexingService;

  @GetMapping("/index")
  public void index() {
    indexingService.indexFromResourceFile();
  }
}
