package uk.ac.ebi.biosamples.search.samples;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
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
    SearchResponse.Builder builder = SearchResponse.newBuilder()
        .addAllAccessions(
            samplePage.stream()
                .map(s -> s.getContent().accession)
                .toList())
        .setSize(samplePage.getSize())
        .setNumber(samplePage.getNumber())
        .setTotalElements(samplePage.getTotalElements())
        .setTotalPages(samplePage.getTotalPages())
        .addAllSort(samplePage.getSort().stream().map(Sort.Order::getProperty).toList());

    if (!samplePage.getContent().isEmpty()) {
      builder.setSearchAfter(samplePage.getContent().getLast().getContent().getUpdate().toString());
    }

    return builder.build();
  }

  private static FacetResponse populateFacetResponse(List<uk.ac.ebi.biosamples.search.samples.facet.Facet> facets) {
    return FacetResponse.newBuilder()
        .addAllFacets(facets.stream()
            .map(f -> Facet.newBuilder()
                .setType(f.type())
                .setField(f.field())
                .setCount(f.count())
                .putAllBuckets(f.buckets())
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
  public void streamSamples(StreamRequest searchRequest, StreamObserver<StreamResponse> responseObserver) {
    log.info("Start of stream for samples................");
    SearchQuery searchQuery = SearchQueryMapper.mapGrpcSearchQuery(searchRequest);
    List<String> searchAfter = searchQuery.getSearchAfter() ;
    SearchPage<Sample> samplePage;
    while (true) {
      searchQuery = searchQuery.toBuilder().searchAfter(searchAfter).build();
      samplePage = searchService.search(searchQuery);
      if (samplePage.getContent().isEmpty()) {
        break;
      }
      for (SearchHit<Sample> sample : samplePage.getContent()) {
        responseObserver.onNext(
            StreamResponse.newBuilder()
                .setAccession(sample.getContent().getAccession())
                .addSearchAfter(sample.getContent().getUpdate().toString())
                .addSearchAfter(sample.getContent().getAccession())
                .build());
        searchAfter = List.of(sample.getContent().getUpdate().toString(), sample.getContent().getAccession());
      }
    }
    log.info("End of stream for samples................");
    responseObserver.onCompleted();
  }

  @Override
  public void getFacets(FacetRequest facetRequest, StreamObserver<FacetResponse> responseObserver) {
    log.info("Calling GRPC method get facets.................");

    SearchQuery searchQuery = SearchQueryMapper.mapGrpcSearchQuery(facetRequest);
    List<uk.ac.ebi.biosamples.search.samples.facet.Facet> facets = facetService.getFacets(searchQuery);
    FacetResponse response = populateFacetResponse(facets);

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
