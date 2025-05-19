package uk.ac.ebi.biosamples_search.samples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {
  private final SamplesRepository samplesRepository;
  private final ObjectMapper objectMapper;
  private final ElasticsearchOperations elasticsearchOperations;

  public Page<Sample> searchSamples() {
//    Page page = PageRequest.of(0, 10).;
//    Iterable<Sample> samples = samplesRepository.findAll();
    Page<Sample> samples = samplesRepository.findAll(Pageable.unpaged());
    return samples;
  }

  public void index() {
    //todo
  }

  public void indexFromResourceFile() {
    log.info("Indexing samples from the local file: samples.json");
    try {
      InputStream is = new ClassPathResource("samples.json").getInputStream();
      List<Sample> samples = objectMapper.readValue(is, new TypeReference<>() {
      });
      samplesRepository.saveAll(samples);
    } catch (IOException e) {
      log.error("Could not read file: {}", e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

//  public List<Sample> searchByAttribute(String key, String value) {
//    QueryBuilder nestedQuery = QueryBuilders.nestedQuery("attributes",
//        QueryBuilders.boolQuery()
//            .must(QueryBuilders.termQuery("attributes.key", key))
//            .must(QueryBuilders.termQuery("attributes.value", value)),
//        ScoreMode.None);
//
//    NativeSearchQuery query = new NativeSearchQueryBuilder()
//        .withQuery(nestedQuery)
//        .build();
//
//    return elasticsearchOperations
//        .search(query, Product.class)
//        .stream()
//        .map(SearchHit::getContent)
//        .toList();
//  }

//  public List<Sample> searchByAttribute(String key, String value) {
//    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
//        .must(QueryBuilders.termQuery("attributes.key", key))
//        .must(QueryBuilders.termQuery("attributes.value.keyword", value)); // Use the keyword sub-field
//
//    NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attributes", boolQuery, org.elasticsearch.search.sort.SortMode.none);
//
//    NativeSearchQuery query = new NativeSearchQueryBuilder()
//        .withQuery(nestedQuery)
//        .build();
//
//    return elasticsearchOperations
//        .search(query, Sample.class)
//        .stream()
//        .map(SearchHit::getContent)
//        .toList();
//  }

}
