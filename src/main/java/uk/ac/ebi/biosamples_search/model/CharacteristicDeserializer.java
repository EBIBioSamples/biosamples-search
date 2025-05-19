package uk.ac.ebi.biosamples_search.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacteristicDeserializer extends StdDeserializer<Set<Attribute>> {

  public CharacteristicDeserializer() {
    this(Set.class);
  }

  private CharacteristicDeserializer(final Class<Set> t) {
    super(t);
  }

  @Override
  public Set<Attribute> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
    final Set<Attribute> attributes = new HashSet<>();
    final Map<String, List<BioSampleAttribute>> characteristics =
        p.readValueAs(new TypeReference<Map<String, List<BioSampleAttribute>>>() {
        });

    for (final String type : characteristics.keySet()) {
      for (final BioSampleAttribute attr : characteristics.get(type)) {
        attributes.add(Attribute.builder().key(type).value(attr.text).build());
      }
    }
    return attributes;
  }

  private static class BioSampleAttribute {
    public String text;
    public List<String> ontologyTerms;
    public String unit;
  }
}
