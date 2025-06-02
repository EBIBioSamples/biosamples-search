package uk.ac.ebi.biosamples.search.samples;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
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
}
