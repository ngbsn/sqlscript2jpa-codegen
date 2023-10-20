package io.github.ngbsn.model.annotations.fieldAnnotations;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class MapsIdAnnotation implements Annotation {
    private String fieldName;

    @Override
    public String toString() {
        return "@MapsId(\"" + fieldName + "\")";
    }
}
