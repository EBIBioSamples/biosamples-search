package uk.ac.ebi.biosamples.search.samples;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.grpc.SearchGrpc;
import uk.ac.ebi.biosamples.search.grpc.SearchRequest;
import uk.ac.ebi.biosamples.search.grpc.SearchResponse;

@Slf4j
@Service
public class SampleServiceGrpc extends SearchGrpc.SearchImplBase {

  @Override
  public void searchSamples(SearchRequest searchRequest, StreamObserver<SearchResponse> responseObserver) {
    log.info("Calling GRPC method search samples.................");
    SearchResponse response = SearchResponse.newBuilder()
        .addAccessions("SAMEA123456")
        .addAccessions("SAMEA123457")
        .build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
