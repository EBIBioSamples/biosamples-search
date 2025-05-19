package uk.ac.ebi.biosamples_search.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.Instant;

//@Component
public class LongToInstantConverter implements Converter<Long, Instant> {

    @Override
    public Instant convert(Long source) {
        return Instant.ofEpochMilli(source);
    }
}