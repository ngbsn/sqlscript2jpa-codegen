package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class GeneratedValueAnnotation implements Annotation {

    @Override
    public String toString() {
        return "@GeneratedValue(strategy = GenerationType.AUTO)";
    }
}
