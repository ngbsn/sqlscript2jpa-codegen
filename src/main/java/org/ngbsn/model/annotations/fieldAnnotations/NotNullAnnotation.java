package org.ngbsn.model.annotations.fieldAnnotations;

import lombok.Builder;
import org.ngbsn.model.annotations.Annotation;

@Builder
public class NotNullAnnotation implements Annotation {
    private boolean notNull;

    @Override
    public String toString() {
        return "@NotNull";
    }
}
