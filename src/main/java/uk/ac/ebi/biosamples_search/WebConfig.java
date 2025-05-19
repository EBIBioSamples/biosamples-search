package uk.ac.ebi.biosamples_search;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ebi.biosamples_search.config.LongToInstantConverter;

import java.time.Instant;

@Configuration
public class WebConfig implements WebMvcConfigurer {

//  @Override
//  public void addFormatters(FormatterRegistry registry) {
//    registry.addConverter(longToInstantConverter());
//  }
//
//  @Bean
//  public Converter<Long, Instant> longToInstantConverter() {
//    return new LongToInstantConverter();
//  }
}
