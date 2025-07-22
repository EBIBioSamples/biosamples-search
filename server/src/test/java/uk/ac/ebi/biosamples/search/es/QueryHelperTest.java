package uk.ac.ebi.biosamples.search.es;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.jupiter.api.Test;
import uk.ac.ebi.biosamples.search.samples.SearchQuery;

import static org.assertj.core.api.Assertions.assertThat;

class QueryHelperTest {

  @Test
  void getSearchQueryWithQuotes_shouldReturnPhraseMatchingQuery() {
    SearchQuery searchQuery = SearchQuery.builder().text("\"Tokyo University\"").build();
    Query esQuery = QueryHelper.getSearchQuery(searchQuery);

    System.out.println(esQuery.toString());
    assertThat(esQuery.toString()).contains("match_phrase");
  }
}