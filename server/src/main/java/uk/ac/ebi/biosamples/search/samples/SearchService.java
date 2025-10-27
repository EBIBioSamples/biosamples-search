package uk.ac.ebi.biosamples.search.samples;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.ac.ebi.biosamples.search.es.QueryHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
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

    SearchAfter searchAfter = searchQuery.getSearchAfter();
    if (isSearchAfterPresentInRequest(searchQuery)) {
      query.setSearchAfter(List.of(searchAfter.update().toString(), searchAfter.accession()));
    }

    return searchForSamplePage(query);
  }

  private PageRequest getPage(SearchQuery searchQuery) {
    List<Sort.Order> sortOrders = getSortOrders(searchQuery);
    int page = isSearchAfterPresentInRequest(searchQuery) ? 0 : searchQuery.getPage();
    int size = searchQuery.getSize() == 0 ? 20 : searchQuery.getSize();
    return PageRequest.of(page, size, Sort.by(sortOrders));
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
    long count = hits.getTotalHits();
    SearchPage<Sample> searchPage = SearchHitSupport.searchPageFor(hits, query.getPageable());
    return new SearchPageWrapper<>(searchPage, count);
  }

  private boolean isSearchAfterPresentInRequest(SearchQuery searchQuery) {
    SearchAfter searchAfter = searchQuery.getSearchAfter();
    return searchAfter != null
        && searchAfter.update() != null
        && !StringUtil.isNullOrEmpty(searchAfter.accession());
  }

  private static class SearchPageWrapper<T> implements SearchPage<T> {
    private final SearchPage<T> delegate;
    private final long totalElements;

    public SearchPageWrapper(SearchPage<T> delegate, long totalElements) {
      this.delegate = delegate;
      this.totalElements = totalElements;
    }

    @Override
    public SearchHits<T> getSearchHits() {
      return delegate.getSearchHits();
    }

    @Override
    public int getTotalPages() {
      return (int) Math.ceil((double) totalElements / delegate.getSize());
    }

    @Override
    public long getTotalElements() {
      return totalElements;
    }

    @Override
    public int getNumber() {
      return delegate.getNumber();
    }

    @Override
    public int getSize() {
      return delegate.getSize();
    }

    @Override
    public int getNumberOfElements() {
      return delegate.getNumberOfElements();
    }

    @Override
    public List<SearchHit<T>> getContent() {
      return delegate.getContent();
    }

    @Override
    public boolean hasContent() {
      return delegate.hasContent();
    }

    @Override
    public Sort getSort() {
      return delegate.getSort();
    }

    @Override
    public boolean isFirst() {
      return delegate.isFirst();
    }

    @Override
    public boolean isLast() {
      return delegate.isLast();
    }

    @Override
    public boolean hasNext() {
      return delegate.hasNext();
    }

    @Override
    public boolean hasPrevious() {
      return delegate.hasPrevious();
    }

    @Override
    public Pageable nextPageable() {
      return delegate.nextPageable();
    }

    @Override
    public Pageable previousPageable() {
      return delegate.previousPageable();
    }

    @Override
    public <U> Page<U> map(Function<? super SearchHit<T>, ? extends U> converter) {
      return delegate.map(converter);
    }

    @Override
    public Iterator<SearchHit<T>> iterator() {
      return delegate.iterator();
    }
  }

}
