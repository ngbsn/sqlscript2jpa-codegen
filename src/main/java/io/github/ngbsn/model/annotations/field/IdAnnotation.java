package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class IdAnnotation implements Annotation {

    @Override
    public String toString() {
        return "@Id";
    }
}
