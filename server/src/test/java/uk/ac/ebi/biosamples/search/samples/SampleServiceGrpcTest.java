package uk.ac.ebi.biosamples.search.samples;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import uk.ac.ebi.biosamples.search.grpc.*;
import uk.ac.ebi.biosamples.search.samples.facet.Facet;
import uk.ac.ebi.biosamples.search.samples.facet.FacetService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleServiceGrpcTest {
  @Mock
  private SearchService searchService;
  @Mock
  private FacetService facetService;
  @Mock
  private StreamObserver<SearchResponse> searchResponseObserver;
  @Mock
  private StreamObserver<StreamResponse> streamResponseObserver;
  @Mock
  private StreamObserver<FacetResponse> facetResponseObserver;
  @Mock
  private SearchPage<Sample> mockSearchPage;
  @Captor
  private ArgumentCaptor<SearchResponse> searchResponseCaptor;
  @Captor
  private ArgumentCaptor<StreamResponse> streamResponseCaptor;
  @Captor
  private ArgumentCaptor<FacetResponse> facetResponseCaptor;
  private SampleServiceGrpc sampleServiceGrpc;

  @BeforeEach
  void setUp() {
    sampleServiceGrpc = new SampleServiceGrpc(searchService, facetService);
  }

  @Test
  void searchSamples_ShouldReturnValidResponse() {
    SearchRequest request = createTestSearchRequest();
    setupMockSearchPage();
    when(searchService.search(any(SearchQuery.class))).thenReturn(mockSearchPage);

    sampleServiceGrpc.searchSamples(request, searchResponseObserver);

    verify(searchResponseObserver).onNext(searchResponseCaptor.capture());
    verify(searchResponseObserver).onCompleted();

    SearchResponse response = searchResponseCaptor.getValue();
    assertSearchResponse(response);
  }

  @Test
  void streamSamples_ShouldReturnValidResponse() {
    StreamRequest request = createTestStreamRequest();
    setupMockSearchPageForStreamResponse();
    when(searchService.search(any(SearchQuery.class))).thenReturn(mockSearchPage);

    sampleServiceGrpc.streamSamples(request, streamResponseObserver);

    verify(streamResponseObserver).onNext(streamResponseCaptor.capture());
    verify(streamResponseObserver).onCompleted();

    StreamResponse response = streamResponseCaptor.getValue();
    assertStreamResponse(response);
  }

  @Test
  void getFacets_ShouldReturnValidResponse() {
    FacetRequest request = createTestFacetRequest();
    List<Facet> mockFacets = createTestFacets();
    when(facetService.getFacets(any(SearchQuery.class))).thenReturn(mockFacets);

    sampleServiceGrpc.getFacets(request, facetResponseObserver);

    verify(facetResponseObserver).onNext(facetResponseCaptor.capture());
    verify(facetResponseObserver).onCompleted();

    FacetResponse response = facetResponseCaptor.getValue();
    assertThat(response.getFacetsList()).hasSize(1);
    assertThat(response.getFacets(0).getType()).isEqualTo("testType");
    assertThat(response.getFacets(0).getField()).isEqualTo("testField");
    assertThat(response.getFacets(0).getCount()).isEqualTo(10L);
  }

  private SearchRequest createTestSearchRequest() {
    return SearchRequest.newBuilder()
        .setText("test query")
        .setSize(10)
        .setNumber(0)
        .build();
  }

  private FacetRequest createTestFacetRequest() {
    return FacetRequest.newBuilder()
        .setText("test query")
        .setSize(10)
        .build();
  }

  private StreamRequest createTestStreamRequest() {
    return StreamRequest.newBuilder()
        .setText("test streaming query")
        .build();
  }

  private void setupMockSearchPage() {
    Sample testSample = Sample.builder().build();
    testSample.setAccession("TEST123");
    testSample.setUpdate(Instant.now());
    SearchHit<Sample> searchHit = getMockSearchHit(testSample);

    when(mockSearchPage.stream()).thenReturn(Stream.of(searchHit));
    when(mockSearchPage.getContent()).thenReturn(List.of(searchHit));
    when(mockSearchPage.getSize()).thenReturn(10);
    when(mockSearchPage.getNumber()).thenReturn(0);
    when(mockSearchPage.getTotalElements()).thenReturn(100L);
    when(mockSearchPage.getTotalPages()).thenReturn(10);
    when(mockSearchPage.getSort()).thenReturn(Sort.by("update"));
  }

  private void setupMockSearchPageForStreamResponse() {
    Sample testSample = Sample.builder().build();
    testSample.setAccession("TEST123");
    testSample.setUpdate(Instant.now());
    SearchHit<Sample> searchHit = getMockSearchHit(testSample);

    when(mockSearchPage.getContent()).thenReturn(List.of(searchHit), List.of(searchHit), List.of());
  }

  private SearchHit<Sample> getMockSearchHit(Sample sample) {
    return new SearchHit<>("1", "1", null, 1.0f, null, null, null, null, null, null, sample);
  }

  private List<Facet> createTestFacets() {
    return List.of(new Facet("testType", "testField", 10L, new HashMap<>()));
  }

  private void assertSearchResponse(SearchResponse response) {
    assertThat(response.getAccessionsList()).hasSize(1);
    assertThat(response.getAccessionsList().getFirst()).isEqualTo("TEST123");
    assertThat(response.getSize()).isEqualTo(10);
    assertThat(response.getNumber()).isEqualTo(0);
    assertThat(response.getTotalElements()).isEqualTo(100L);
    assertThat(response.getTotalPages()).isEqualTo(10);
    assertThat(response.getSortList()).contains("update");
  }

  private void assertStreamResponse(StreamResponse response) {
    assertThat(response.getAccession()).isNotBlank();
    assertThat(response.getSearchAfter()).isNotNull();
  }
}