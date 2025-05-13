package uk.ac.ebi.biosamples_search.samples;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SamplesController {
  private final SampleModelAssembler modelAssembler;
  private final SamplesService samplesService;

  public SamplesController(SampleModelAssembler modelAssembler, SamplesService samplesService) {
    this.modelAssembler = modelAssembler;
    this.samplesService = samplesService;
  }

  @GetMapping("/test")
  public String postTestMessage() {
    return "works ok";
  }

  @GetMapping("/samples/{accession}")
  public EntityModel<Sample> getSample(@PathVariable String accession) {
    //TODO
    return modelAssembler.toModel(new Sample(accession, "hello", "hello"));
  }

  @GetMapping("/search")
  public CollectionModel<EntityModel<Sample>> searchSamples() {
    return CollectionModel.of(samplesService.searchSamples().stream().map(modelAssembler::toModel).toList());
  }

  @PostMapping("/search")
  public CollectionModel<EntityModel<Sample>> searchSamples(@RequestBody SampleSearchQuery query) {
    return CollectionModel.of(samplesService.searchSamples().stream().map(modelAssembler::toModel).toList());
  }

  @GetMapping("/index")
  public void index() {
    samplesService.index();
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
