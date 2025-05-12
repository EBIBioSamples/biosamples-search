package uk.ac.ebi.biosamples_search.samples;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SamplesRepository extends ElasticsearchRepository<Sample, String> {

}
