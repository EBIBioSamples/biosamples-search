package uk.ac.ebi.biosamples.search.samples;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SamplesRepository extends ElasticsearchRepository<Sample, String> {

}
