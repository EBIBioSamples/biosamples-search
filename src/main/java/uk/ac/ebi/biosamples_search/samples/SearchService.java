package uk.ac.ebi.biosamples_search.samples;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
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
import uk.ac.ebi.biosamples_search.samples.filter.SearchFilter;

import java.util.List;

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
    Query matchQuery = getTextMatchQuery(searchQuery);
    Query filterQuery = getFilterQuery(searchQuery);
    NativeQuery query = getEsNativeQuery(pageRequest, matchQuery, filterQuery);
    return searchForSamplePage(query);
  }

  private PageRequest getPage(SearchQuery searchQuery) {
    return PageRequest.of(
        searchQuery.getPage(),
        searchQuery.getSize(),
        Sort.by("update").descending());
  }

  private Query getTextMatchQuery(SearchQuery searchQuery) {
    String searchText = searchQuery.getText();
    return MatchQuery.of(m -> m
        .field("sample_full_text")
        .query(searchText)
    )._toQuery();
  }

  private Query getFilterQuery(SearchQuery searchQuery) {
    List<Query> filterQueries = searchQuery.getFilters().stream()
        .map(SearchFilter::getQuery).toList();

    if (filterQueries.isEmpty()) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    return BoolQuery.of(b -> b.must(filterQueries))._toQuery();
  }

  private NativeQuery getEsNativeQuery(PageRequest page, Query match, Query filter) {
    Query query = BoolQuery.of(b -> b
        .must(match)
        .filter(filter)
    )._toQuery();

    return NativeQuery.builder()
        .withQuery(query)
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
