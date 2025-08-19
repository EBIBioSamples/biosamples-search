package uk.ac.ebi.biosamples.search.samples.proto;

import uk.ac.ebi.biosamples.search.grpc.FacetRequest;
import uk.ac.ebi.biosamples.search.grpc.SearchRequest;
import uk.ac.ebi.biosamples.search.grpc.StreamRequest;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.samples.SortOrder;
import uk.ac.ebi.biosamples.search.samples.filter.FilterMapper;

import java.util.List;

public class SearchQueryMapper {
  private static final int STREAM_PAGE_SIZE = 500;
  private static final List<String> STREAM_SORT = List.of("update", "accession.keyword");

  private SearchQueryMapper() {
  }

  public static SearchQuery mapGrpcSearchQuery(SearchRequest searchRequest) {
    return SearchQuery.builder()
        .text(searchRequest.getText())
        .filters(searchRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .size(searchRequest.getSize())
        .page(searchRequest.getNumber())
        .sort(searchRequest.getSortList().stream().map(s -> new SortOrder(s, null)).toList())
        .build();
  }

  public static SearchQuery mapGrpcSearchQuery(StreamRequest streamRequest) {
    return SearchQuery.builder()
        .text(streamRequest.getText())
        .filters(streamRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .size(STREAM_PAGE_SIZE)
        .sort(streamRequest.getSortList().stream().map(s -> new SortOrder(s, null)).toList())
//        .sort(STREAM_SORT.stream().map(s -> new SortOrder(s, null)).toList())
        .searchAfter(streamRequest.getSearchAfterList())
        .build();
  }

  public static SearchQuery mapGrpcSearchQuery(FacetRequest facetRequest) {
    return SearchQuery.builder()
        .text(facetRequest.getText())
        .filters(facetRequest.getFiltersList().stream().map(FilterMapper::mapGrpcFilterToSearchFilter).toList())
        .facets(facetRequest.getFacetsList())
        .size(STREAM_PAGE_SIZE)
        .build();
  }

}
