package uk.ac.ebi.biosamples.search.samples.filter;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = AccessionSearchFilter.class, name = "acc"),
    @JsonSubTypes.Type(value = SraAccessionSearchFilter.class, name = "sAcc"), //SRA Accession
    @JsonSubTypes.Type(value = NameSearchFilter.class, name = "name"),
    @JsonSubTypes.Type(value = WebinIdSearchFilter.class, name = "webin"),
    @JsonSubTypes.Type(value = DateRangeSearchFilter.class, name = "dt"),
    @JsonSubTypes.Type(value = AttributeSearchFilter.class, name = "attr"),
    @JsonSubTypes.Type(value = RelationshipSearchFilter.class, name = "rel"), //reverse?
    @JsonSubTypes.Type(value = ExternalRefSearchFilter.class, name = "extd"),
    @JsonSubTypes.Type(value = StructuredDataSearchFilter.class, name = "sdata")
})
public sealed interface SearchFilter permits AccessionSearchFilter, SraAccessionSearchFilter, NameSearchFilter,
    WebinIdSearchFilter, DateRangeSearchFilter, AttributeSearchFilter, RelationshipSearchFilter,
    ExternalRefSearchFilter, StructuredDataSearchFilter {

  Query getQuery();
}
