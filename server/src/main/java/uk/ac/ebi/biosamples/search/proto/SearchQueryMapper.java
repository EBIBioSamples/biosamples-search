package uk.ac.ebi.biosamples.search.proto;

import com.google.protobuf.Timestamp;
import uk.ac.ebi.biosamples.search.grpc.FacetRequest;
import uk.ac.ebi.biosamples.search.grpc.SearchAfter;
import uk.ac.ebi.biosamples.search.grpc.SearchRequest;
import uk.ac.ebi.biosamples.search.grpc.StreamRequest;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.samples.SortOrder;
import uk.ac.ebi.biosamples.search.filter.FilterMapper;

import java.time.Instant;
import java.util.List;

public class SearchQueryMapper {
  private static final int STREAM_PAGE_SIZE = 500;
  private static final List<String> STREAM_SORT = List.of("update", "accession.keyword");

  private SearchQueryMapper() {
  }

  public static SearchQuery mapFromGrpcSearchQuery(SearchRequest searchRequest) {
    return SearchQuery.builder()
        .text(searchRequest.getText())
        .filters(searchRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .size(searchRequest.getSize())
        .page(searchRequest.getNumber())
        .sort(searchRequest.getSortList().stream().map(s -> new SortOrder(s, null)).toList())
        .searchAfter(mapFromGrpcSearchAfter(searchRequest.getSearchAfter()))
        .build();
  }

  public static SearchQuery mapFromGrpcSearchQuery(StreamRequest streamRequest) {
    return SearchQuery.builder()
        .text(streamRequest.getText())
        .filters(streamRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .size(STREAM_PAGE_SIZE)
        .sort(streamRequest.getSortList().stream().map(s -> new SortOrder(s, null)).toList()) //todo direction
//        .sort(STREAM_SORT.stream().map(s -> new SortOrder(s, null)).toList())
        .searchAfter(mapFromGrpcSearchAfter(streamRequest.getSearchAfter()))
        .build();
  }

  public static SearchQuery mapFromGrpcSearchQuery(FacetRequest facetRequest) {
    return SearchQuery.builder()
        .text(facetRequest.getText())
        .filters(facetRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .facets(facetRequest.getFacetsList())
        .size(STREAM_PAGE_SIZE)
        .build();
  }

  public static SearchAfter mapToGrpcSearchAfter(uk.ac.ebi.biosamples.search.samples.SearchAfter searchAfter) {
    return SearchAfter.newBuilder()
        .setUpdate(convertToTimestamp(searchAfter.update()))
        .setAccession(searchAfter.accession())
        .build();
  }

  public static uk.ac.ebi.biosamples.search.samples.SearchAfter mapFromGrpcSearchAfter(SearchAfter searchAfter) {
    return new uk.ac.ebi.biosamples.search.samples.SearchAfter(convertFromTimestamp(searchAfter.getUpdate()), searchAfter.getAccession());
  }

  public static Timestamp convertToTimestamp(Instant instant) {
    if (instant == null) {
      return null;
    }
    return Timestamp.newBuilder()
        .setSeconds(instant.getEpochSecond())
        .setNanos(instant.getNano())
        .build();
  }

  public static Instant convertFromTimestamp(Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }
    return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
  }

}
