package io.github.ngbsn.model.annotations.field;

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
