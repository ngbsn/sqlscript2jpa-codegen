package io.github.ngbsn.model.annotations.field;

import io.github.ngbsn.model.annotations.Annotation;
import lombok.Builder;

@Builder
public class NotNullAnnotation implements Annotation {
    private boolean notNull;

    @Override
    public String toString() {
        return "@NotNull";
    }
}
