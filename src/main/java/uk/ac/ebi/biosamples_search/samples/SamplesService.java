package uk.ac.ebi.biosamples_search.samples;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SamplesService {
  private final SamplesRepository samplesRepository;

  public Page<Sample> searchSamples() {
    Iterable<Sample> samples = samplesRepository.findAll();
    return (Page<Sample>) samples;
  }

  public void index() {
    List<Sample> samples = Arrays.asList(
        Sample.builder().accession("a1").name("Name 1").description("Description 1").build(),
        Sample.builder().accession("a2").name("Name 2").description("Description 2").build(),
        Sample.builder().accession("a3").name("Name 3").description("Description 3").build()
    );
    samplesRepository.saveAll(samples);
  }
}
