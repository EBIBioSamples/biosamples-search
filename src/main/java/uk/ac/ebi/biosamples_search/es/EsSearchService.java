package uk.ac.ebi.biosamples_search.es;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples_search.samples.Sample;
import uk.ac.ebi.biosamples_search.samples.SearchQuery;
import uk.ac.ebi.biosamples_search.samples.SamplesRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsSearchService {
  private final SamplesRepository samplesRepository;
  private final ElasticsearchOperations elasticsearchOperations;

  public Page<Sample> searchSamples() {
//    Page page = PageRequest.of(0, 10).;
//    Iterable<Sample> samples = samplesRepository.findAll();
    Page<Sample> samples = samplesRepository.findAll(Pageable.unpaged());
    return samples;
  }

  public SearchHits<Sample> searchSamples(SearchQuery searchQuery) {

    Pageable page = Pageable.ofSize(searchQuery.getSize());

//    Query query = NativeQuery.builder()
////        .withAggregation("lastNames", Aggregation.of(a -> a
////            .terms(ta -> ta.field("lastName").size(10))))
//        .withQuery(q -> q
//            .match(m -> m
//                .field("accession")
//                .query("SAMD00000001")
//            )
//        )
//        .withPageable(page)
//        .build();

    Query matchQuery = MatchQuery.of(m -> m
        .field("status")
        .query("PUBLIC")
    )._toQuery();

    Query nestedFilterQuery = NestedQuery.of(n -> n
        .path("characteristics")
        .query(q -> q
            .bool(b -> b
                .must(List.of(
                    TermQuery.of(t -> t
                        .field("characteristics.key")
                        .value("env_medium")
                    )._toQuery(),
                    TermQuery.of(t -> t
                        .field("characteristics.value")
                        .value("soil")
                    )._toQuery()
                ))
            )
        )
    )._toQuery();

    Query finalQuery = BoolQuery.of(b -> b
        .must(matchQuery)
        .filter(nestedFilterQuery)
    )._toQuery();

    NativeQuery query = NativeQuery.builder()
        .withQuery(finalQuery)
        .build();


    log.info("Generated Elasticsearch Query: {}", query.getQuery().toString());
    SearchHits<Sample> searchHits = elasticsearchOperations.search(query, Sample.class);
    return searchHits;
  }

  public SearchPage<Sample> search(String nameQuery, int page, int size) {
    Query matchQuery = MatchQuery.of(m -> m
        .field("status")
        .query(nameQuery)
    )._toQuery();

    Query nestedFilterQuery = NestedQuery.of(n -> n
        .path("characteristics")
        .query(q -> q
            .bool(b -> b
                .must(List.of(
                    TermQuery.of(t -> t.field("characteristics.key").value("env_medium"))._toQuery(),
                    TermQuery.of(t -> t.field("characteristics.value").value("soil"))._toQuery()
                ))
            )
        )
    )._toQuery();

    Query finalQuery = BoolQuery.of(b -> b
        .must(matchQuery)
        .filter(nestedFilterQuery)
    )._toQuery();

    Aggregation attributeKeyFacet = Aggregation.of(a -> a
        .nested(n -> n.path("characteristics"))
        .aggregations("by_key", subAgg -> subAgg
            .terms(t -> t.field("characteristics.key.keyword"))
        )
    );

    PageRequest pageRequest = PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("update").descending());

    NativeQuery query = NativeQuery.builder()
        .withQuery(finalQuery)
        .withPageable(pageRequest)
//        .withSort(org.springframework.data.domain.Sort.by("update").ascending())
        .withAggregation("attribute_keys", attributeKeyFacet)
        .build();

    log.info("Generated Elasticsearch Query: {}", query.getQuery().toString());
    SearchHits<Sample> hits = elasticsearchOperations.search(query, Sample.class);

    Map<String, Aggregate> aggregations = getAggregations(hits);
    AggregationsContainer<?> aggContainer = hits.getAggregations();
    List<Aggregation> ags = (List<Aggregation>) aggContainer.aggregations();

    return SearchHitSupport.searchPageFor(hits, pageRequest);
  }

  public Map<String, Aggregate> getAggregations(SearchHits<Sample> hits) {
    Map<String, Aggregate> aggregationsMap = new HashMap<>();

    return aggregationsMap;
  }

}
