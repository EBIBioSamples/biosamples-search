package uk.ac.ebi.biosamples.search.samples;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import uk.ac.ebi.biosamples.search.IntegrationTestConfiguration;
import uk.ac.ebi.biosamples.search.TestDependencyContainers;
import uk.ac.ebi.biosamples.search.grpc.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration.class)
@ImportTestcontainers(TestDependencyContainers.class)
public class SampleServiceGrpcIT {

  @Test
  void searchSamples_shouldReturnFirstAccessionPage() {
    runTestWithSetupAndTearDown((stub) -> {
      SearchRequest request = SearchRequest.newBuilder().setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(733);
    });
  }

  @Test
  void searchSamplesWithText_shouldReturnSampleAccessions() {
    runTestWithSetupAndTearDown((stub) -> {
      String text = "Dermacentor andersoni";
      SearchRequest request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(1);
    });
  }

  @Test
  void streamSamplesWithText_shouldReturnSampleAccessionsStream() {
    runTestWithSetupAndTearDown((stub) -> {
      String text = "\"Tokyo University of Agriculture and Technology\"";
      StreamRequest request = StreamRequest.newBuilder().setText(text).build();
      Iterator<StreamResponse> streamingResponse = stub.streamSamples(request);

      List<String> accessions = new ArrayList<>();
      while (streamingResponse.hasNext()) {
        StreamResponse response = streamingResponse.next();
        accessions.add(response.getAccession());
      }

      assertThat(accessions.size()).isEqualTo(3);
    });
  }

  @Test
  void searchSamplesWithManuallyConstructedPublicFilter_shouldReturnOnlyPublicSampleAccessions() {
    runTestWithSetupAndTearDown((stub) -> {
      Filter filter = Filter.newBuilder().setDateRange(
          DateRangeFilter.newBuilder()
              .setField(DateRangeFilter.DateField.RELEASE)
              .setTo("2024-01-19T23:08:46.037Z")
              .build()).build();
      SearchRequest request = SearchRequest.newBuilder().addAllFilters(List.of(filter)).setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(732);
    });
  }

  @Test
  void searchSamplesWithPublicFilter_shouldReturnOnlyPublicSampleAccessions() {
    runTestWithSetupAndTearDown((stub) -> {
      Filter publicFilter = Filter.newBuilder()
          .setPublic(PublicFilter.newBuilder().build())
          .build();
      SearchRequest request = SearchRequest.newBuilder().addAllFilters(List.of(publicFilter)).setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(732);
    });
  }

  @Test
  void searchSamplesWithPublicFilterAndAuthentication_shouldReturnPublicAndAccountPrivateSampleAccessions() {
    runTestWithSetupAndTearDown((stub) -> {
      Filter publicFilter = Filter.newBuilder()
          .setPublic(PublicFilter.newBuilder().setWebinId("Webin-40894").build())
          .build();
      SearchRequest request = SearchRequest.newBuilder().addAllFilters(List.of(publicFilter)).setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(733);
    });
  }

  @Test
  void searchSamplesWithTextAndMixOfFilters_shouldReturnRelevantSampleAccessions() {
    runTestWithSetupAndTearDown((stub) -> {
      String text = "ReCoDID";
      Filter publicFilter = Filter.newBuilder()
          .setPublic(PublicFilter.newBuilder().setWebinId("Webin-40757").build())
          .build();
      Filter attributeFilter = Filter.newBuilder()
          .setAttribute(AttributeFilter.newBuilder()
              .setField("organism")
              .addAllValues(List.of("Homo sapiens"))
              .build())
          .build();
      Filter relationshipFilter = Filter.newBuilder()
          .setRelationship(RelationshipFilter.newBuilder()
              .setType("derived from")
              .setTarget("SAMEA12928720")
              .build())
          .build();
      SearchRequest request = SearchRequest.newBuilder()
          .setText(text)
          .addAllFilters(List.of(publicFilter, attributeFilter, relationshipFilter))
          .setSize(10)
          .build();
      SearchResponse response = stub.searchSamples(request);
      assertThat(response.getTotalElements()).isEqualTo(3);
    });
  }

  @Test
  void streamSamplesWithAdvancedTextSearch_shouldReturnCorrectSampleAccessionsStream() {
    runTestWithSetupAndTearDown((stub) -> {
      String text = "Tokyo AND University";
      SearchRequest request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      SearchResponse response = stub.searchSamples(request);
      long tokyoAndUniversityCount = response.getTotalElements();

      text = "Tokyo OR University";
      request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      response = stub.searchSamples(request);
      long tokyoOrUniversityCount = response.getTotalElements();

      text = "Tokyo AND NOT University";
      request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      response = stub.searchSamples(request);
      long tokyoOnlyCount = response.getTotalElements();

      text = "NOT Tokyo AND University";
      request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      response = stub.searchSamples(request);
      long universityOnlyCount = response.getTotalElements();

      text = "University";
      request = SearchRequest.newBuilder().setText(text).setSize(10).build();
      response = stub.searchSamples(request);
      long universityCount = response.getTotalElements();

      assertThat(tokyoOnlyCount + universityCount).isEqualTo(tokyoOrUniversityCount);
      assertThat(tokyoOnlyCount + tokyoAndUniversityCount + universityOnlyCount).isEqualTo(tokyoOrUniversityCount);
    });
  }

  void runTestWithSetupAndTearDown(Consumer<SearchGrpc.SearchBlockingStub> test) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
        .usePlaintext()
        .build();
    SearchGrpc.SearchBlockingStub searchBlockingStub = SearchGrpc.newBlockingStub(channel);
    try {
      test.accept(searchBlockingStub);
    } finally {
      channel.shutdown();
    }
  }
}
