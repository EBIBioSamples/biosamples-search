package uk.ac.ebi.biosamples_search.samples;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SampleModelAssembler implements RepresentationModelAssembler<Sample, EntityModel<Sample>> {
  @Override
  public EntityModel<Sample> toModel(Sample sample) {
    return EntityModel.of(sample, linkTo(methodOn(SamplesController.class).getSample(sample.getAccession())).withSelfRel());
  }

}
