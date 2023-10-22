package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class GeneratedValueAnnotation implements Annotation {

    @Override
    public String toString() {
        return "@GeneratedValue(strategy = GenerationType.AUTO)";
    }
}
