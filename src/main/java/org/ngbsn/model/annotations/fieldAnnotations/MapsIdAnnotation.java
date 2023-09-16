package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class MapsIdAnnotation implements Annotation {
    private String fieldName;

    @Override
    public String toString() {
        return "@MapsId(\"" + fieldName + "\")";
    }
}
