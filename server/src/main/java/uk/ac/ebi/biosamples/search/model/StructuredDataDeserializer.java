package uk.ac.ebi.biosamples.search.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StructuredDataDeserializer extends StdDeserializer<Set<StructuredData>> {

  public StructuredDataDeserializer() {
    this(Set.class);
  }

  private StructuredDataDeserializer(final Class<Set> t) {
    super(t);
  }

  @Override
  public Set<StructuredData> deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
    final Set<StructuredData> structuredData = new HashSet<>();
    final List<StructuredDataTable> tables =
        p.readValueAs(new TypeReference<List<StructuredDataTable>>() {});

    for (StructuredDataTable table : tables) {
      for (Map<String, StructuredDataEntry> content : table.content()) {
        for (Map.Entry<String, StructuredDataEntry> entry : content.entrySet()) {
          structuredData.add(new StructuredData(table.type(), entry.getKey(), entry.getValue().value()));
        }
      }
    }
    return structuredData;
  }
}
