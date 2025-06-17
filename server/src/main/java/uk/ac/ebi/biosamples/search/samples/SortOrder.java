package uk.ac.ebi.biosamples.search.samples;

import org.springframework.data.domain.Sort;

public record SortOrder(String field, Sort.Direction direction) {
}
