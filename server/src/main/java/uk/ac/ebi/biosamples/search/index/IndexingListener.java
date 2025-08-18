package uk.ac.ebi.biosamples.search.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.ac.ebi.biosamples.search.samples.Sample;
import uk.ac.ebi.biosamples.search.samples.SamplesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingListener {
  private static final int BATCH_SIZE = 1000;
  private static final BlockingQueue<Sample> INDEXING_QUEUE = new LinkedBlockingQueue<>();
  private final SamplesRepository samplesRepository;
  private final ObjectMapper objectMapper;

  public void receiveMessage(String message) {
    try {
      Sample sample = objectMapper.readValue(message, Sample.class);
      boolean queued = INDEXING_QUEUE.offer(sample);
      if (!queued) {
        log.error("Failed to queue sample for indexing, accession: {}. Queue is full.", sample.getAccession());
      }
    } catch (JsonProcessingException e) {
      log.error("Failed to parse sample from message: {}", message, e);
    }
  }

  @Scheduled(initialDelay = 60, fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
  void indexSamplesInBatches() {
    while (INDEXING_QUEUE.size() >= BATCH_SIZE) {
      indexBatch();
    }
    indexRemaining();
  }

  private void indexBatch() {
    log.info("Indexing samples in batches of {}", BATCH_SIZE);
    List<Sample> samples = new ArrayList<>();
    INDEXING_QUEUE.drainTo(samples, BATCH_SIZE);
    samplesRepository.saveAll(samples);
  }

  @PreDestroy
  private void indexRemaining() {
    if (!INDEXING_QUEUE.isEmpty()) {
      log.info("Indexing {} samples and flushing indexing cache", INDEXING_QUEUE.size());
      List<Sample> samples = new ArrayList<>();
      INDEXING_QUEUE.drainTo(samples);
      samplesRepository.saveAll(samples);
    }
  }
}
