package uk.ac.ebi.biosamples_search.samples;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SamplesController {
  private final SearchService samplesService;
  private final IndexingService indexingService;

  @GetMapping("/samples/{accession}")
  public ResponseEntity<Sample> getSample(@PathVariable String accession) {
    //TODO
    return ResponseEntity.ok(Sample.builder().build());
  }

  @GetMapping("/search")
  public PagedModel<EmbeddedWrapper> searchSamples() {
    Page<Sample> samples = samplesService.searchSamples();
    //EmbeddedWrappers to rename default _embedded serialisation format
    EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
    List<EmbeddedWrapper> elements = samples.stream()
        .map(s -> wrappers.wrap(s.getAccession(), LinkRelation.of("accessions")))
        .toList();

    return PagedModel.of(
        elements,
        new PagedModel.PageMetadata(
            samples.getSize(),
            samples.getNumber(),
            samples.getTotalElements(),
            samples.getTotalPages()
        ),
        Link.of("search/self").withSelfRel(),
        Link.of("search/next/cursor").withRel("next"));
  }

  @PostMapping("/search")
  public List<Sample> searchSamples(@RequestBody SampleSearchQuery query) {
    SearchHits<Sample> samples = samplesService.searchSamples(query);
    return samples.stream().map(SearchHit::getContent).toList();
  }

  @PostMapping("/search1")
  public List<Sample> searchSamples1(@RequestBody SampleSearchQuery query) {
    return samplesService.search("PUBLIC", 1, 10).stream()
        .map(SearchHit::getContent).toList();
  }

//  @PostMapping("/search")
//  public CollectionModel<EntityModel<Sample>> searchSamples(@RequestBody SampleSearchQuery query) {
//    return CollectionModel.of(samplesService.searchSamples().stream().map(modelAssembler::toModel).toList());
//  }

  @GetMapping("/index")
  public void index() {
    indexingService.indexFromResourceFile();
  }

//  @CrossOrigin(methods = RequestMethod.GET)
//  @GetMapping(produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
//  public ResponseEntity<CollectionModel<EntityModel<Sample>>> searchHal(
//      @RequestParam(name = "text", required = false) final String text,
//      @RequestParam(name = "filter", required = false) final String[] filter,
//      @RequestParam(name = "cursor", required = false) String cursor,
//      @RequestParam(name = "page", required = false) final Integer page,
//      @RequestParam(name = "size", required = false) final Integer size,
//      @RequestParam(name = "sort", required = false) final String[] sort,
//      @RequestParam(name = "applyCurations", required = false, defaultValue = "true")
//          final boolean applyCurations) {

}
