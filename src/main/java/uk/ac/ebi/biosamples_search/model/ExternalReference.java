package uk.ac.ebi.biosamples_search.model;

import java.util.Set;

public record ExternalReference(String url, Set<String> duo, String archive, String accession) {

}
