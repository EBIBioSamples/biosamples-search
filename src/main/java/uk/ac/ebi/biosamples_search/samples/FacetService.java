package uk.ac.ebi.biosamples_search.samples;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
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
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples_search.samples.facet.Facet;
import uk.ac.ebi.biosamples_search.samples.filter.SearchFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacetService {
  private final SamplesRepository samplesRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  public Page<Sample> searchSamples() {
    return samplesRepository.findAll(Pageable.unpaged());
  }

  public List<Facet> search(SearchQuery searchQuery) {
    PageRequest pageRequest = getPage(searchQuery);
    Query matchQuery = getTextMatchQuery(searchQuery);
    Query filterQuery = getFilterQuery(searchQuery);
    Aggregation aggregation = getAggregations(searchQuery);
    NativeQuery query = getEsNativeQuery(pageRequest, matchQuery, filterQuery, aggregation);
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

  private Aggregation getAggregations(SearchQuery searchQuery) {

    Aggregation aggregation = Aggregation.of(a -> a
        .nested(n -> n.path("characteristics"))
        .aggregations("by_key", a1 -> a1
            .terms(t -> t.field("characteristics.key.keyword"))
            .aggregations("by_value", a2 -> a2
                .terms(t2 -> t2.field("characteristics.value.keyword"))
            )
        )
    );

    return aggregation;
  }

  private NativeQuery getEsNativeQuery(PageRequest page, Query match, Query filter, Aggregation aggregation) {
    Query query = BoolQuery.of(b -> b
        .must(match)
        .filter(filter)
    )._toQuery();

    return NativeQuery.builder()
        .withQuery(query)
        .withPageable(page)
        .withAggregation("facets", aggregation)
        .withSort(page.getSort())
        .build();
  }

  private List<Facet> searchForSamplePage(NativeQuery query) {
    log.info("Generated Elasticsearch Query: {}", query.getAggregations().toString());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);

    ElasticsearchAggregations aggregations = (ElasticsearchAggregations) hits.getAggregations();
    assert aggregations != null;
//    List<StringTermsBucket> buckets = aggregations.aggregationsAsMap().get("facets").aggregation().getAggregate().sterms().buckets().array();
    aggregations.aggregationsAsMap().get("facets").aggregation().getAggregate().nested().aggregations().get("by_key");

//    if (hits.hasAggregations()) {
//      AggregationsContainer<?> aggregationsContainer = hits.getAggregations();
//      if (aggregationsContainer != null && aggregationsContainer.aggregations() != null) {
////        ((List<Aggregate>) aggregationsContainer.aggregations()).stream().filter(agg -> "facets".equals(agg.))
//      }
//    }
/// ////////////////////////////////////
    List<Facet> facets = new ArrayList<>();
    ElasticsearchAggregations elasticAggs = (ElasticsearchAggregations) hits.getAggregations();

    if (elasticAggs != null) {
      Map<String, ElasticsearchAggregation> aggMap = elasticAggs.aggregationsAsMap();

      NestedAggregate nestedAggResult = aggMap.get("facets").aggregation().getAggregate().nested();

      List<StringTermsBucket> stBuckets = nestedAggResult.aggregations().get("by_key").sterms().buckets().array();
      for (StringTermsBucket bucket : stBuckets) {
        long keyCount = bucket.docCount();
        String keyKey = bucket.key().stringValue();
        Map<String, Long> facetBuckets = new HashMap<>();
        facets.add(new Facet("", keyKey, keyCount, facetBuckets));
        List<StringTermsBucket> valueBuckets = bucket.aggregations().get("by_value").sterms().buckets().array();
        for (StringTermsBucket valueBucket : valueBuckets) {
          long valueCount = valueBucket.docCount();
          String valueKey = valueBucket.key().stringValue();
          facetBuckets.put(valueKey, valueCount);
        }
      }
    }

    return facets;
//    return SearchHitSupport.searchPageFor(hits, query.getPageable());
  }

}
