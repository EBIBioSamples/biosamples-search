package uk.ac.ebi.biosamples.search.samples;

import java.time.Instant;

public record SearchAfter(Instant update, String accession) {
}
