package uk.ac.ebi.biosamples_search.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.ac.ebi.biosamples_search.utils.ExternalReferenceUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExternalReferenceDeserializer extends StdDeserializer<Set<ExternalReference>> {

  public ExternalReferenceDeserializer() {
    this(Set.class);
  }

  private ExternalReferenceDeserializer(final Class<Set> t) {
    super(t);
  }

  public static ExternalReference populateFieldsFromUrl(ExternalReference ref) {
    return new ExternalReference(
        ref.url(),
        ref.duo(),
        ExternalReferenceUtils.getNickname(ref),
        ExternalReferenceUtils.getDataId(ref).orElse(""));
  }

  @Override
  public Set<ExternalReference> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
    List<ExternalReference> refs = p.readValueAs(new TypeReference<List<ExternalReference>>() {
    });

    return refs.stream()
        .map(ExternalReferenceDeserializer::populateFieldsFromUrl)
        .collect(Collectors.toSet());
  }
}
