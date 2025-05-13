package uk.ac.ebi.biosamples_search.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SamplesServiceTest {
  @InjectMocks
  SamplesService samplesService;


  @Test
  void sampleSearchShouldReturnPaginatedSampleList() {
    Page<Sample> samples = samplesService.searchSamples();
    assertThat(samples.getTotalElements()).isEqualTo(0);
  }

}