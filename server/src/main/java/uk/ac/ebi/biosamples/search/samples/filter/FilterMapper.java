package uk.ac.ebi.biosamples.search.samples.filter;

import uk.ac.ebi.biosamples.search.grpc.*;

public class FilterMapper {
  public static SearchFilter mapGrpcFilterToSearchFilter(Filter grpcFilter) {
    return switch (grpcFilter.getValueCase()) {
      case PUBLIC -> getPublicSearchFilter(grpcFilter.getPublic());
      case ACCESSION -> getAccessionSearchFilter(grpcFilter.getAccession());
      case SRAACCESSION -> getSraAccessionSearchFilter(grpcFilter.getSraAccession());
      case NAME -> getNameSearchFilter(grpcFilter.getName());
      case WEBIN -> getWebinIdSearchFilter(grpcFilter.getWebin());
      case DOMAIN -> getDomainSearchFilter(grpcFilter.getDomain());
      case DATERANGE -> getDateRangeSearchFilter(grpcFilter.getDateRange());
      case ATTRIBUTE -> getAttributeSearchFilter(grpcFilter.getAttribute());
      case RELATIONSHIP -> getRelationshipSearchFilter(grpcFilter.getRelationship());
      case EXTERNAL -> getExternalRefSearchFilter(grpcFilter.getExternal());
      case STRUCTUREDDATA -> getStructuredDataSearchFilter(grpcFilter.getStructuredData());
      case VALUE_NOT_SET -> throw new IllegalArgumentException("Filter dataType not set");
    };
  }

  private static PublicSearchFilter getPublicSearchFilter(PublicFilter filter) {
    return new PublicSearchFilter(filter.getWebinId());
  }

  private static AccessionSearchFilter getAccessionSearchFilter(AccessionFilter filter) {
    return new AccessionSearchFilter(filter.getAccession());
  }

  private static SraAccessionSearchFilter getSraAccessionSearchFilter(SraAccessionFilter filter) {
    return new SraAccessionSearchFilter(filter.getAccession());
  }

  private static NameSearchFilter getNameSearchFilter(NameFilter filter) {
    return new NameSearchFilter(filter.getName());
  }

  private static WebinIdSearchFilter getWebinIdSearchFilter(WebinIdFilter filter) {
    return new WebinIdSearchFilter(filter.getWebinId());
  }

  private static DomainSearchFilter getDomainSearchFilter(DomainFilter filter) {
    return new DomainSearchFilter(filter.getDomain());
  }

  private static DateRangeSearchFilter getDateRangeSearchFilter(DateRangeFilter filter) {
    return new DateRangeSearchFilter(
        DateRangeSearchFilter.DateField.valueOf(filter.getField().name()),
        filter.getFrom(),
        filter.getTo()
    );
  }

  private static AttributeSearchFilter getAttributeSearchFilter(AttributeFilter filter) {
    return new AttributeSearchFilter(filter.getField(), filter.getValuesList());
  }

  private static RelationshipSearchFilter getRelationshipSearchFilter(RelationshipFilter filter) {
    return new RelationshipSearchFilter(filter.getType(), filter.getSource(), filter.getTarget());
  }

  private static ExternalRefSearchFilter getExternalRefSearchFilter(ExternalRefFilter filter) {
    return new ExternalRefSearchFilter(filter.getArchive(), filter.getAccession());
  }

  private static StructuredDataSearchFilter getStructuredDataSearchFilter(StructuredDataFilter filter) {
    return new StructuredDataSearchFilter(filter.getType(), filter.getField(), filter.getValue());
  }

}
