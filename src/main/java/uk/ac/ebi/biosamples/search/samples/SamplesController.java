package uk.ac.ebi.biosamples.search.samples;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.biosamples.search.index.IndexingService;
import uk.ac.ebi.biosamples.search.samples.facet.Facet;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SamplesController {
  private final SearchService samplesService;
  private final IndexingService indexingService;
  private final FacetService facetService;

  @GetMapping("/search")
  public PagedModel<EmbeddedWrapper> searchSamples(
      @RequestParam String q, @RequestParam int page, @RequestParam int size) {
    // q = text + filter -> base64 encoded
    Page<Sample> samples = samplesService.searchSamples();
    //EmbeddedWrappers to rename default _embedded serialisation format
    EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
    List<EmbeddedWrapper> elements = samples.stream()
        .map(s -> wrappers.wrap(s.getAccession(), LinkRelation.of("accessions")))
        .toList();

    List<Link> links = populateLinks(samples);

    return PagedModel.of(
        elements,
        new PagedModel.PageMetadata(
            samples.getSize(),
            samples.getNumber(),
            samples.getTotalElements(),
            samples.getTotalPages()
        ),
        links);
  }

  @PostMapping("/search")
  public PagedModel<EmbeddedWrapper> searchSamples(@RequestBody SearchQuery query) {
    SearchPage<Sample> searchPage = samplesService.search(query);

    EmbeddedWrappers wrappers = new EmbeddedWrappers(false);
    List<EmbeddedWrapper> elements = searchPage.stream()
        .map(s -> wrappers.wrap(s.getContent(), LinkRelation.of("accessions")))
        .toList();

    List<Link> links = populateLinks(searchPage);

    return PagedModel.of(
        elements,
        new PagedModel.PageMetadata(
            searchPage.getSize(),
            searchPage.getNumber(),
            searchPage.getTotalElements(),
            searchPage.getTotalPages()
        ),
        links);
  }

  @PostMapping("/facet")
  public List<Facet> facet(@RequestBody SearchQuery query) {
    return facetService.getFacets(query);
  }

  private List<Link> populateLinks(Page<?> searchPage) {
    //todo we have 2 options: base64 encode filters or store search context temporarily
    // (we will not use uri templates)
    List<Link> links = new ArrayList<>();
    String basePostSearchPath = "/search";

    links.add(Link.of(UriComponentsBuilder.fromPath(basePostSearchPath)
        .queryParam("page", searchPage.getNumber())
        .queryParam("size", searchPage.getSize())
        .build().toUriString(), IanaLinkRelations.SELF));

    if (searchPage.getTotalPages() > 0) {
      links.add(Link.of(UriComponentsBuilder.fromPath(basePostSearchPath)
          .queryParam("page", 0)
          .queryParam("size", searchPage.getSize())
          .build().toUriString(), IanaLinkRelations.FIRST));

      links.add(Link.of(UriComponentsBuilder.fromPath(basePostSearchPath)
          .queryParam("page", searchPage.getTotalPages() - 1)
          .queryParam("size", searchPage.getSize())
          .build().toUriString(), IanaLinkRelations.LAST));
    }

    if (searchPage.hasPrevious()) {
      links.add(Link.of(UriComponentsBuilder.fromPath(basePostSearchPath)
          .queryParam("page", searchPage.getNumber() - 1)
          .queryParam("size", searchPage.getSize())
          .build().toUriString(), IanaLinkRelations.PREV));
    }

    if (searchPage.hasNext()) {
      links.add(Link.of(UriComponentsBuilder.fromPath(basePostSearchPath)
          .queryParam("page", searchPage.getNumber() + 1)
          .queryParam("size", searchPage.getSize())
          .build().toUriString(), IanaLinkRelations.NEXT));
    }
    return links;
  }

}
