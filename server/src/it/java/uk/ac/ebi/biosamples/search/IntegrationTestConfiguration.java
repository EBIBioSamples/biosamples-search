package uk.ac.ebi.biosamples.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import uk.ac.ebi.biosamples.search.samples.Sample;
import uk.ac.ebi.biosamples.search.samples.SamplesRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class IntegrationTestConfiguration {
  @Autowired
  SamplesRepository samplesRepository;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  ElasticsearchOperations elasticsearchOperations;

  @PostConstruct
  public void setup() {
    createIndex();
    indexFromResourceFile();
  }

  private void createIndex() {
    IndexCoordinates index = IndexCoordinates.of("samples");
    IndexOperations indexOps = elasticsearchOperations.indexOps(index);

    try {
      InputStream is = new ClassPathResource("index.json").getInputStream();
      String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      Document mapping = Document.create().fromJson(json);

      Map<String, Object> settings = ImmutableMap.of("number_of_shards", 1, "number_of_replicas", 1);

      if (indexOps.exists()) {
        indexOps.delete();
      }
      indexOps.create(settings, mapping);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void indexFromResourceFile() {
    try {
      InputStream is = new ClassPathResource("samples.json").getInputStream();
      List<Sample> samples = objectMapper.readValue(is, new TypeReference<>() {
      });
      samplesRepository.saveAll(samples);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
