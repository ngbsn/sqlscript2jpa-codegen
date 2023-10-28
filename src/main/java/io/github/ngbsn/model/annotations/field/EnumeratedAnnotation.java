package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import jakarta.persistence.EnumType;
import lombok.Builder;

@Builder
public class EnumeratedAnnotation implements Annotation {
    private EnumType value;

    @Override
    public String toString() {
        return "@Enumerated(EnumType." + value + ")";
    }
}
