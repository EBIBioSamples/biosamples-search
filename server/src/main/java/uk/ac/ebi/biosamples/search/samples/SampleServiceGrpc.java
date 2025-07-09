package uk.ac.ebi.biosamples.search.samples;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.grpc.*;
import uk.ac.ebi.biosamples.search.samples.proto.SearchQueryMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleServiceGrpc extends SearchGrpc.SearchImplBase {
  private final SearchService searchService;
  private final FacetService facetService;

  private static SearchResponse populateSearchResponse(SearchPage<Sample> samplePage) {
    return SearchResponse.newBuilder()
        .addAllAccessions(
            samplePage.stream()
                .map(s -> s.getContent().accession)
                .toList())
        .setSize(samplePage.getSize())
        .setNumber(samplePage.getNumber())
        .setTotalElements(samplePage.getTotalElements())
        .setTotalPages(samplePage.getTotalPages())
        .setSearchAfter(samplePage.getContent().getLast().getContent().getUpdate().toString())
        .addAllSort(samplePage.getSort().stream().map(Sort.Order::getProperty).toList())
        .build();
  }

  private static FacetResponse populateFacetResponse(List<uk.ac.ebi.biosamples.search.samples.facet.Facet> facets) {
    return FacetResponse.newBuilder()
        .addAllFacets(facets.stream()
            .map(f -> Facet.newBuilder()
                .setType(f.type())
                .setField(f.field())
                .setCount(f.count())
                .build()).toList())
        .build();
  }

  @Override
  public void searchSamples(SearchRequest searchRequest, StreamObserver<SearchResponse> responseObserver) {
    log.info("Calling GRPC method search samples.................");

    SearchQuery searchQuery = SearchQueryMapper.mapGrpcSearchQuery(searchRequest);
    SearchPage<Sample> samplePage = searchService.search(searchQuery);
    SearchResponse response = populateSearchResponse(samplePage);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void streamSamples(SearchRequest searchRequest, StreamObserver<SearchResponse> responseObserver) {
    log.info("Calling GRPC method stream samples.................");

    // todo validate streaming params

    SearchQuery searchQuery = SearchQueryMapper.mapGrpcSearchQuery(searchRequest);
    SearchPage<Sample> samplePage = searchService.search(searchQuery);
    SearchResponse response = populateSearchResponse(samplePage);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getFacets(SearchRequest searchRequest, StreamObserver<FacetResponse> responseObserver) {
    log.info("Calling GRPC method get facets.................");

    SearchQuery searchQuery = SearchQueryMapper.mapGrpcSearchQuery(searchRequest);
    List<uk.ac.ebi.biosamples.search.samples.facet.Facet> facets = facetService.getFacets(searchQuery);
    FacetResponse response = populateFacetResponse(facets);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
