package uk.ac.ebi.biosamples.search.es;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticConfig /*extends ElasticsearchConfiguration*/ {

//  @Override
//  public ClientConfiguration clientConfiguration() {
//    return ClientConfiguration.builder().connectedToLocalhost().withBasicAuth("elastic", "elastic").build();
//  }
}
