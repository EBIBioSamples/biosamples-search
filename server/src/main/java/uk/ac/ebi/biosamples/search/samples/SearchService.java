package uk.ac.ebi.biosamples.search.samples;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.es.QueryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
  private final SamplesRepository samplesRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  public Page<Sample> searchSamples() {
    return samplesRepository.findAll(Pageable.unpaged());
  }

  public SearchPage<Sample> search(SearchQuery searchQuery) {
    PageRequest pageRequest = getPage(searchQuery);
    Query esSearchQuery = QueryHelper.getSearchQuery(searchQuery);
    NativeQuery query = getEsNativeQuery(pageRequest, esSearchQuery);
    return searchForSamplePage(query);
  }

  private PageRequest getPage(SearchQuery searchQuery) {
    List<Sort.Order> sortOrders = getSortOrders(searchQuery);
    return PageRequest.of(searchQuery.getPage(), searchQuery.getSize(), Sort.by(sortOrders));
  }

  private List<Sort.Order> getSortOrders(SearchQuery searchQuery) {
    Stream<Sort.Order> userDefinedSortStream =
        (searchQuery.getSort() == null || searchQuery.getSort().isEmpty()) ? Stream.empty()
            : searchQuery.getSort().stream().map(s -> new Sort.Order(s.direction(), s.field()));
    Stream<Sort.Order> defaultSortStream = Stream.of(
        Sort.Order.desc("update"),
        Sort.Order.asc("accession.keyword")
    );
    return Stream.concat(userDefinedSortStream, defaultSortStream).toList();
  }

  private NativeQuery getEsNativeQuery(PageRequest page, Query esSearchQuery) {
    return NativeQuery.builder()
        .withQuery(esSearchQuery)
        .withPageable(page)
        .build();
  }

  private SearchPage<Sample> searchForSamplePage(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    return SearchHitSupport.searchPageFor(hits, query.getPageable());
  }

}
