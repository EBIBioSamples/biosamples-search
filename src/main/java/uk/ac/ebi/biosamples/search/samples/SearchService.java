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
    return PageRequest.of(
        searchQuery.getPage(),
        searchQuery.getSize(),
        Sort.by("update").descending());
  }

  private NativeQuery getEsNativeQuery(PageRequest page, Query esSearchQuery) {
    return NativeQuery.builder()
        .withQuery(esSearchQuery)
        .withPageable(page)
        .withSort(page.getSort())
        .build();
  }

  private SearchPage<Sample> searchForSamplePage(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery().toString());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    return SearchHitSupport.searchPageFor(hits, query.getPageable());
  }

}
