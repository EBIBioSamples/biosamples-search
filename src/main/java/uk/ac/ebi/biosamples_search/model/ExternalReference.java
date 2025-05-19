/*
* Copyright 2021 EMBL - European Bioinformatics Institute
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
* file except in compliance with the License. You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the
* License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
* CONDITIONS OF ANY KIND, either express or implied. See the License for the
* specific language governing permissions and limitations under the License.
*/
package uk.ac.ebi.biosamples_search.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalReference implements Comparable<ExternalReference> {
  private final String url;
  @JsonIgnore private final String hash;
  private final SortedSet<String> duo;

  private ExternalReference(final String url, final String hash, final SortedSet<String> duo) {
    this.url = url;
    this.hash = hash;
    this.duo = duo;
  }

  @Override
  public int compareTo(final ExternalReference other) {
    if (other == null) {
      return 1;
    }

    if (!url.equals(other.url)) {
      return url.compareTo(other.url);
    }

    if (duo == other.duo) {
      return 0;
    } else if (other.duo == null) {
      return 1;
    } else if (duo == null) {
      return -1;
    }

    if (!duo.equals(other.duo)) {
      if (duo.size() < other.duo.size()) {
        return -1;
      } else if (duo.size() > other.duo.size()) {
        return 1;
      } else {
        final Iterator<String> thisIt = duo.iterator();
        final Iterator<String> otherIt = other.duo.iterator();
        while (thisIt.hasNext() && otherIt.hasNext()) {
          final int val = thisIt.next().compareTo(otherIt.next());
          if (val != 0) {
            return val;
          }
        }
      }
    }
    return 0;
  }

  public static ExternalReference build(String url, SortedSet<String> duo) {
    final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);
    final UriComponents uriComponents = uriComponentsBuilder.build().normalize();

    url = uriComponents.toUriString();


    return new ExternalReference(url, null, duo);
  }

  @JsonCreator
  public static ExternalReference build(@JsonProperty("url") final String url) {
    return build(url, new TreeSet<>());
  }
}
