package uk.ac.ebi.biosamples.search.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredDataDeserializerTest {
  private static final String SD_JSON = """
      [
        {
          "webinSubmissionAccountId": "Webin-51990",
          "type": "SAMPLE",
          "schema": null,
          "content": [
            {
              "marker": {
                "value": "Index PCR cycles",
                "iri": null
              },
              "measurement": {
                "value": "6",
                "iri": null
              },
              "partner": {
                "value": "UCPH",
                "iri": "https://www.ku.dk/english/"
              }
            },
            {
              "marker": {
                "value": "Body site",
                "iri": null
              },
              "measurement": {
                "value": "caecum content",
                "iri": null
              }
            },
            {
              "marker": {
                "value": "Sequencing company",
                "iri": null
              },
              "measurement": {
                "value": "BGI",
                "iri": null
              },
              "partner": {
                "value": "UCPH",
                "iri": "https://www.ku.dk/english/"
              }
            },
            {
              "marker": {
                "value": "Lab Process ID",
                "iri": null
              },
              "measurement": {
                "value": "LPC00042",
                "iri": null
              },
              "partner": {
                "value": "UCPH",
                "iri": "https://www.ku.dk/english/"
              }
            }
          ]
        },
        {
          "webinSubmissionAccountId": "Webin-51990",
          "type": "TREATMENT",
          "schema": null,
          "content": [
            {
              "marker": {
                "value": "Treatment name",
                "iri": null
              },
              "measurement": {
                "value": "Control",
                "iri": null
              }
            },
            {
              "marker": {
                "value": "Treatment code",
                "iri": null
              },
              "measurement": {
                "value": "CC",
                "iri": null
              }
            }
          ]
        },
        {
          "webinSubmissionAccountId": "Webin-51990",
          "type": "HISTOLOGY",
          "schema": null,
          "content": []
        }
      ]
      """;

  private static ObjectMapper objectMapper;

  @BeforeAll
  static void setUp() {
    objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.registerModule(new SimpleModule().addDeserializer(Set.class, new StructuredDataDeserializer()));
  }

  @Test
  void structuredDataJson_shouldDeserializeCorrectly() throws IOException {
    Set<StructuredData> structuredData = objectMapper.readValue(SD_JSON,
        objectMapper.getTypeFactory().constructCollectionType(Set.class, StructuredData.class));
    assertThat(structuredData).isNotNull();
  }

  @Test
  void structuredDataJson_shouldDeserializeAllRecords() throws IOException {
    Set<StructuredData> structuredData = objectMapper.readValue(SD_JSON,
        objectMapper.getTypeFactory().constructCollectionType(Set.class, StructuredData.class));
    assertThat(structuredData.size()).isEqualTo(13);
  }

}