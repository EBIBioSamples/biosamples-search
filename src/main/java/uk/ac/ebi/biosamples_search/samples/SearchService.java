package uk.ac.ebi.biosamples_search.samples;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
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
import uk.ac.ebi.biosamples_search.samples.filter.AttributeSearchFilter;

import java.util.ArrayList;
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
    PageRequest pageRequest = PageRequest.of(
        searchQuery.getPage(),
        searchQuery.getSize(),
        Sort.by("update").descending());
    return pageRequest;
  }

  private Query getTextMatchQuery(SearchQuery searchQuery) {
    String searchText = searchQuery.getText();
    Query matchQuery = MatchQuery.of(m -> m
        .field("sample_full_text")
        .query(searchText)
    )._toQuery();
    return matchQuery;
  }

  private Query getFilterQuery(SearchQuery searchQuery) {
//    List<Query> filterQueries = new ArrayList<>();


    List<Query> filterQueries = searchQuery.getFilters().stream()
        .filter(f -> f instanceof AttributeSearchFilter)
        .map(f -> {
          AttributeSearchFilter filter = (AttributeSearchFilter) f;
          return filter.getQuery();
//          return NestedQuery.of(n -> n
//              .path("characteristics")
//              .query(q -> q
//                  .bool(b -> b
//                      .must(List.of(
//                          TermQuery.of(t -> t.field("characteristics.key.keyword").value(filter.getField()))._toQuery(),
//                          TermQuery.of(t -> t.field("characteristics.value.keyword").value(filter.getValue()))._toQuery()
//                      ))
//                  )
//              )
//          )._toQuery();
        }).toList();

//    Query nestedFilterQuery = NestedQuery.of(n -> n
//        .path("characteristics")
//        .query(q -> q
//            .bool(b -> b
//                .must(List.of(
//                    TermQuery.of(t -> t.field("characteristics.key").value("env_medium"))._toQuery(),
//                    TermQuery.of(t -> t.field("characteristics.value").value("soil"))._toQuery()
//                ))
//            )
//        )
//    )._toQuery();
//    filterQueries.add(nestedFilterQuery);
//
//    nestedFilterQuery = NestedQuery.of(n -> n
//        .path("characteristics")
//        .query(q -> q
//            .bool(b -> b
//                .must(List.of(
//                    TermQuery.of(t -> t.field("characteristics.key").value("rel_to_oxygen"))._toQuery(),
//                    TermQuery.of(t -> t.field("characteristics.value").value("aerobe"))._toQuery()
//                ))
//            )
//        )
//    )._toQuery();
//    filterQueries.add(nestedFilterQuery);

    if (filterQueries.isEmpty()) {
      return MatchAllQuery.of(m -> m)._toQuery();
    }

    return BoolQuery.of(b -> b.must(filterQueries))._toQuery();
  }

  private NativeQuery getEsNativeQuery(PageRequest page, Query match, Query filter) {
    Query finalQuery = BoolQuery.of(b -> b
        .must(match)
        .filter(filter)
    )._toQuery();

    NativeQuery query = NativeQuery.builder()
        .withQuery(finalQuery)
        .withPageable(page)
        .withSort(page.getSort())
        .build();

    return query;
  }

  private SearchPage<Sample> searchForSamplePage(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getQuery().toString());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);
    return SearchHitSupport.searchPageFor(hits, query.getPageable());
  }

}
