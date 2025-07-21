package uk.ac.ebi.biosamples.search.model;

import java.util.Set;

public record ExternalReference(String url, Set<String> duo, String archive, String accession) {
}
