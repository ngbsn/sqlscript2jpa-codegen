package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class IdAnnotation implements Annotation {

    @Override
    public String toString() {
        return "@Id";
    }
}
