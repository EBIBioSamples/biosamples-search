package uk.ac.ebi.biosamples_search.samples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
  @Mock
  SamplesRepository samplesRepository;

  @InjectMocks
  SearchService samplesService;


  @Test
  void sampleSearchShouldReturnPaginatedSampleList() {
    when(samplesRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
    Page<Sample> samples = samplesService.searchSamples();
    assertThat(samples.getTotalElements()).isEqualTo(0);
  }

}