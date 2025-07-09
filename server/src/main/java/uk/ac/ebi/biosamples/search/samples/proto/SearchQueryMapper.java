package uk.ac.ebi.biosamples.search.samples.proto;

import uk.ac.ebi.biosamples.search.grpc.SearchRequest;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;
import uk.ac.ebi.biosamples.search.samples.SortOrder;
import uk.ac.ebi.biosamples.search.samples.filter.FilterMapper;

public class SearchQueryMapper {
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
}
