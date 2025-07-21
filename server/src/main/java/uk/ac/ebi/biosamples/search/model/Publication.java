package uk.ac.ebi.biosamples.search.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Publication(String doi, @JsonProperty("pubmed_id") String pubmedId) {
}
